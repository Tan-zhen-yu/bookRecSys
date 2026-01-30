package com.example.bookrec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookrec.common.Result;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.service.IBookInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/recommend")
public class RecommendResultController {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private IBookInfoService bookInfoService;
    @Autowired // 注入 Redis
    private RedisTemplate<String, Object> redisTemplate;

    private final String PYTHON_API_URL = "http://localhost:5000/recommend?userId=";

    @GetMapping("/user")
    public Result<List<BookInfo>> getRecommend(@RequestParam Long userId) {
        String cacheKey = "rec:user:" + userId;

        // 1. 查 Redis (逻辑不变，Redis里存的JSON也会包含 matchScore)
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            List<BookInfo> cachedList = (List<BookInfo>) redisTemplate.opsForValue().get(cacheKey);
            return Result.success(cachedList);
        }

        // 2. 调 Python
        System.out.println("调用 Python 算法...");

        // map: Key=BookId, Value=Score
        Map<Long, Integer> scoreMap = new HashMap<>();
        List<Long> bookIds = new ArrayList<>();

        try {
            // 注意：这里返回值类型变了，是 List<Map>
            ResponseEntity<Map> response = restTemplate.getForEntity(PYTHON_API_URL + userId, Map.class);
            Map body = response.getBody();

            if (body != null && (int) body.get("code") == 200) {
                // 解析 Python 返回的 [{"book_id":1, "score":98}, ...]
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) body.get("data");

                for (Map<String, Object> item : dataList) {
                    Long bookId = Long.valueOf(item.get("book_id").toString());
                    Integer score = Integer.valueOf(item.get("score").toString());

                    bookIds.add(bookId);
                    scoreMap.put(bookId, score);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<BookInfo> bookList;
        if (!bookIds.isEmpty()) {
            bookList = bookInfoService.listByIds(bookIds);

            // --- 核心步骤：把分数填回 BookInfo 对象 ---
            for (BookInfo book : bookList) {
                // 从 map 里取出分数放入对象
                if (scoreMap.containsKey(book.getId())) {
                    book.setMatchScore(scoreMap.get(book.getId()));
                }
            }

            // 重新按分数排序 (因为 listByIds 可能会乱序)
            bookList.sort((b1, b2) -> b2.getMatchScore().compareTo(b1.getMatchScore()));

        } else {
            // 兜底逻辑 (热门图书)
            LambdaQueryWrapper<BookInfo> query = new LambdaQueryWrapper<>();
            query.orderByDesc(BookInfo::getRatingCount).last("limit 10");
            bookList = bookInfoService.list(query);
            // 兜底的书没有 matchScore，或者你可以设一个默认值比如 90
        }

        // 3. 存 Redis
        if (!bookList.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, bookList, 1, TimeUnit.HOURS);
        }

        return Result.success(bookList);
    }

    // RecommendController.java

    @PostMapping("/keywords")
    public Result<List<Map<String, Object>>> getKeywords(@RequestBody Map<String, String> params) {
        String text = params.get("text");
        // 简单做个缓存 Key (MD5一下text或者直接用bookId更好，这里简化处理)
        // 实际项目中建议把 keywords 存到 book_info 表的一个字段里，不要每次都算

        String pythonUrl = "http://localhost:5000/keywords";
        try {
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("text", text);

            ResponseEntity<Map> response = restTemplate.postForEntity(pythonUrl, requestMap, Map.class);
            Map body = response.getBody();
            if (body != null && (int) body.get("code") == 200) {
                return Result.success((List<Map<String, Object>>) body.get("data"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success(new ArrayList<>());
    }

    // 获取关联图书（看了这本书的人也喜欢...）
    @GetMapping("/related/{bookId}")
    public Result<List<BookInfo>> getRelatedBooks(@PathVariable Long bookId) {
        String url = "http://localhost:5000/related?bookId=" + bookId;

        // 存储 ID 和 分数的映射
        Map<Long, Integer> scoreMap = new HashMap<>();
        List<Long> bookIds = new ArrayList<>();

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map body = response.getBody();
            if (body != null && (int) body.get("code") == 200) {
                // 解析新结构 [{"book_id": 101, "score": 95}, ...]
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) body.get("data");

                for (Map<String, Object> item : dataList) {
                    Long id = Long.valueOf(item.get("book_id").toString());
                    Integer score = Integer.valueOf(item.get("score").toString());

                    bookIds.add(id);
                    scoreMap.put(id, score);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果 Python 没算出结果，走兜底策略 (同分类)
        if (bookIds.isEmpty()) {
            BookInfo currentBook = bookInfoService.getById(bookId);
            if (currentBook != null) {
                LambdaQueryWrapper<BookInfo> query = new LambdaQueryWrapper<>();
                query.eq(BookInfo::getCategoryId, currentBook.getCategoryId())
                        .ne(BookInfo::getId, bookId)
                        .last("limit 6");
                List<BookInfo> categoryBooks = bookInfoService.list(query);
                // 兜底的书没有算法分，就不设置 matchScore，前端不显示标签即可
                return Result.success(categoryBooks);
            }
        }

        // 有结果，查询详情并填入分数
        List<BookInfo> bookList = bookInfoService.listByIds(bookIds);
        for (BookInfo book : bookList) {
            if (scoreMap.containsKey(book.getId())) {
                book.setMatchScore(scoreMap.get(book.getId()));
            }
        }

        // 按分数排序
        bookList.sort((b1, b2) -> {
            Integer s1 = b1.getMatchScore() == null ? 0 : b1.getMatchScore();
            Integer s2 = b2.getMatchScore() == null ? 0 : b2.getMatchScore();
            return s2.compareTo(s1);
        });

        return Result.success(bookList);
    }
}