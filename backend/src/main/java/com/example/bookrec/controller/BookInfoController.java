package com.example.bookrec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.bookrec.common.Result;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.service.IBookInfoService;
import com.example.bookrec.service.CacheService;
import com.example.bookrec.service.BloomFilterService;
import com.example.bookrec.resilience.CircuitBreakerAspect.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book")
public class BookInfoController {

    @Autowired
    private IBookInfoService bookInfoService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private BloomFilterService bloomFilterService;

    /**
     * 图书分页列表 & 搜索筛选
     * GET /book/page?pageNum=1&pageSize=10&keyword=Java&categoryId=1
     */
    @GetMapping("/page")
    public Result<Page<BookInfo>> findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,    // 搜索关键词
            @RequestParam(required = false) Long categoryId) { // 分类筛选

        // 1. 构建分页对象
        Page<BookInfo> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件
        LambdaQueryWrapper<BookInfo> query = new LambdaQueryWrapper<>();

        // 如果有关键词，模糊查询 书名 OR 作者
        if (StringUtils.isNotBlank(keyword)) {
            query.and(w -> w.like(BookInfo::getTitle, keyword)
                    .or()
                    .like(BookInfo::getAuthor, keyword));
        }

        // 如果有分类ID，精确查询
        if (categoryId != null) {
            query.eq(BookInfo::getCategoryId, categoryId);
        }

        // 按创建时间倒序（新书在前）
        query.orderByDesc(BookInfo::getCreateTime);

        // 3. 执行查询
        Page<BookInfo> result = bookInfoService.page(page, query);

        return Result.success(result);
    }

    /**
     * 图书详情 - 带完整缓存防护
     * GET /book/{id}
     */
    @GetMapping("/{id}")
    public Result<BookInfo> getDetail(@PathVariable Long id) {
        // 1. 布隆过滤器预检查（防止缓存穿透）
        if (!bloomFilterService.mightContain(id)) {
            return Result.error("图书不存在");
        }
        
        // 2. 使用击穿防护获取数据（防止缓存击穿）
        BookInfo book = cacheService.getBookWithCacheBreakdownProtection(id);
        if (book == null) {
            return Result.error("图书不存在");
        }
        return Result.success(book);
    }

    // 管理员添加图书 (简单版)
    @PostMapping("/save")
    public Result<Boolean> saveBook(@RequestBody BookInfo bookInfo) {
        boolean result = bookInfoService.saveOrUpdate(bookInfo);
        if (result && bookInfo.getId() != null) {
            // 添加到布隆过滤器
            bloomFilterService.add(bookInfo.getId());
            // 清除可能存在的缓存
            cacheService.deleteBookCache(bookInfo.getId());
        }
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteBook(@PathVariable Long id) {
        boolean result = bookInfoService.removeById(id);
        if (result) {
            // 从布隆过滤器中删除（实际需要重建）
            bloomFilterService.delete(id);
            // 清除缓存
            cacheService.deleteBookCache(id);
        }
        return Result.success(result);
    }

    // BookInfoController.java

    // 1. 高分榜 (Rating Avg 降序) - 带缓存
    @GetMapping("/rank/rating")
    @CircuitBreaker(
        key = "book_rating_rank", 
        failureThreshold = 3, 
        timeout = 30,
        minRequestThreshold = 5
    )
    public Result<List<BookInfo>> getRatingRank() {
        String cacheKey = "book:rank:rating";
        List<BookInfo> cachedResult = cacheService.getListFromCache(cacheKey, BookInfo.class);
        if (cachedResult != null) {
            return Result.success(cachedResult);
        }
        
        LambdaQueryWrapper<BookInfo> query = new LambdaQueryWrapper<>();
        query.ge(BookInfo::getRatingCount, 5) // 评分人数至少5人(防止只有1人打5分就排第一)
                .orderByDesc(BookInfo::getRatingAvg)
                .last("limit 10");
        List<BookInfo> result = bookInfoService.list(query);
        
        // 缓存30分钟
        cacheService.setListToCache(cacheKey, result, 30);
        return Result.success(result);
    }

    // 2. 热度榜 (Rating Count 降序) - 带缓存
    @GetMapping("/rank/hot")
    @CircuitBreaker(
        key = "book_hot_rank", 
        failureThreshold = 3, 
        timeout = 30,
        minRequestThreshold = 5
    )
    public Result<List<BookInfo>> getHotRank() {
        String cacheKey = "book:rank:hot";
        List<BookInfo> cachedResult = cacheService.getListFromCache(cacheKey, BookInfo.class);
        if (cachedResult != null) {
            return Result.success(cachedResult);
        }
        
        LambdaQueryWrapper<BookInfo> query = new LambdaQueryWrapper<>();
        query.orderByDesc(BookInfo::getRatingCount)
                .last("limit 10");
        List<BookInfo> result = bookInfoService.list(query);
        
        // 缓存30分钟
        cacheService.setListToCache(cacheKey, result, 30);
        return Result.success(result);
    }

    // 3. 新书榜 (Create Time 降序) - 带缓存
    @GetMapping("/rank/new")
    @CircuitBreaker(
        key = "book_new_rank", 
        failureThreshold = 3, 
        timeout = 30,
        minRequestThreshold = 5
    )
    public Result<List<BookInfo>> getNewRank() {
        String cacheKey = "book:rank:new";
        List<BookInfo> cachedResult = cacheService.getListFromCache(cacheKey, BookInfo.class);
        if (cachedResult != null) {
            return Result.success(cachedResult);
        }
        
        LambdaQueryWrapper<BookInfo> query = new LambdaQueryWrapper<>();
        query.orderByDesc(BookInfo::getCreateTime)
                .last("limit 10");
        List<BookInfo> result = bookInfoService.list(query);
        
        // 缓存30分钟
        cacheService.setListToCache(cacheKey, result, 30);
        return Result.success(result);
    }
}