package com.example.bookrec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.bookrec.common.Result;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.service.IBookInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book")
public class BookInfoController {

    @Autowired
    private IBookInfoService bookInfoService;

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
     * 图书详情
     * GET /book/{id}
     */
    @GetMapping("/{id}")
    public Result<BookInfo> getDetail(@PathVariable Long id) {
        BookInfo book = bookInfoService.getById(id);
        if (book == null) {
            return Result.error("图书不存在");
        }
        return Result.success(book);
    }

    // 管理员添加图书 (简单版)
    @PostMapping("/save")
    public Result<Boolean> saveBook(@RequestBody BookInfo bookInfo) {
        return Result.success(bookInfoService.saveOrUpdate(bookInfo));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteBook(@PathVariable Long id) {
        return Result.success(bookInfoService.removeById(id));
    }

    // BookInfoController.java

    // 1. 高分榜 (Rating Avg 降序)
    @GetMapping("/rank/rating")
    public Result<List<BookInfo>> getRatingRank() {
        LambdaQueryWrapper<BookInfo> query = new LambdaQueryWrapper<>();
        query.ge(BookInfo::getRatingCount, 5) // 评分人数至少5人(防止只有1人打5分就排第一)
                .orderByDesc(BookInfo::getRatingAvg)
                .last("limit 10");
        return Result.success(bookInfoService.list(query));
    }

    // 2. 热度榜 (Rating Count 降序)
    @GetMapping("/rank/hot")
    public Result<List<BookInfo>> getHotRank() {
        LambdaQueryWrapper<BookInfo> query = new LambdaQueryWrapper<>();
        query.orderByDesc(BookInfo::getRatingCount)
                .last("limit 10");
        return Result.success(bookInfoService.list(query));
    }

    // 3. 新书榜 (Create Time 降序)
    @GetMapping("/rank/new")
    public Result<List<BookInfo>> getNewRank() {
        LambdaQueryWrapper<BookInfo> query = new LambdaQueryWrapper<>();
        query.orderByDesc(BookInfo::getCreateTime)
                .last("limit 10");
        return Result.success(bookInfoService.list(query));
    }
}