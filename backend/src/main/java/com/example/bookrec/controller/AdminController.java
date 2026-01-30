package com.example.bookrec.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.bookrec.common.Result;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.service.IBookCategoryService;
import com.example.bookrec.service.IBookInfoService;
import com.example.bookrec.service.IUserBookRatingService;
import com.example.bookrec.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IBookInfoService bookInfoService;
    @Autowired
    private IUserBookRatingService ratingService;
    @Autowired
    private IBookCategoryService categoryService;

    // 1. 首页统计卡片数据
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> map = new HashMap<>();
        map.put("userCount", userService.count()); // 用户总数
        map.put("bookCount", bookInfoService.count()); // 图书总数
        map.put("ratingCount", ratingService.count()); // 评分总数
        // 假设这里有一个访问量统计，暂时写死或者去查日志表
        map.put("visitCount", 1024);
        return Result.success(map);
    }

    // 2. 图书分类占比 (饼图)
    @GetMapping("/categoryStats")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        // SQL逻辑：SELECT c.name, COUNT(b.id) as value FROM book_category c LEFT JOIN book_info b ON c.id = b.category_id GROUP BY c.id
        // 这里为了演示简单，用 Java 内存处理（数据量大时建议写 XML 自定义 SQL）
        List<Map<String, Object>> result = new ArrayList<>();

        categoryService.list().forEach(category -> {
            QueryWrapper<BookInfo> query = new QueryWrapper<>();
            query.eq("category_id", category.getId());
            long count = bookInfoService.count(query);

            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getName());
            item.put("value", count);
            result.add(item);
        });
        return Result.success(result);
    }

    // 3. 热门图书排行 (柱状图)
    @GetMapping("/hotBooks")
    public Result<List<BookInfo>> getHotBooks() {
        QueryWrapper<BookInfo> query = new QueryWrapper<>();
        // 按评分人数降序，取前10
        query.orderByDesc("rating_count").last("limit 10");
        query.select("title", "rating_count", "rating_avg"); // 只查需要的字段
        return Result.success(bookInfoService.list(query));
    }
}