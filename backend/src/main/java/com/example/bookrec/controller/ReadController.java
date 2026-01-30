package com.example.bookrec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookrec.common.Result;
import com.example.bookrec.entity.BookChapter;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.service.IBookChapterService;
import com.example.bookrec.service.IBookInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/read")
public class ReadController {

    @Autowired
    private IBookChapterService chapterService;
    @Autowired
    private IBookInfoService bookInfoService;

    /**
     * 1. 获取目录列表
     * GET /read/catalog?bookId=101
     */
    @GetMapping("/catalog")
    public Result<Map<String, Object>> getCatalog(@RequestParam Long bookId) {
        // 查书名
        BookInfo book = bookInfoService.getById(bookId);
        if (book == null) return Result.error("图书不存在");

        // 查章节列表 (注意：只查 ID 和 Title，不查 Content，提高性能)
        LambdaQueryWrapper<BookChapter> query = new LambdaQueryWrapper<>();
        query.eq(BookChapter::getBookId, bookId)
                .select(BookChapter::getId, BookChapter::getTitle, BookChapter::getChapterNum) // 关键优化
                .orderByAsc(BookChapter::getChapterNum);

        List<BookChapter> chapters = chapterService.list(query);

        Map<String, Object> map = new HashMap<>();
        map.put("bookTitle", book.getTitle());
        map.put("chapters", chapters);

        return Result.success(map);
    }

    /**
     * 2. 获取章节内容
     * GET /read/chapter?chapterId=1
     */
    @GetMapping("/chapter")
    public Result<Map<String, Object>> getChapter(@RequestParam Long chapterId) {
        BookChapter current = chapterService.getById(chapterId);
        if (current == null) return Result.error("章节不存在");

        // 计算上一章 ID
        // 逻辑：找同一本书，chapterNum 比当前小 的最大那个
        LambdaQueryWrapper<BookChapter> prevQuery = new LambdaQueryWrapper<>();
        prevQuery.eq(BookChapter::getBookId, current.getBookId())
                .lt(BookChapter::getChapterNum, current.getChapterNum())
                .orderByDesc(BookChapter::getChapterNum)
                .last("limit 1")
                .select(BookChapter::getId);
        BookChapter prev = chapterService.getOne(prevQuery);

        // 计算下一章 ID
        // 逻辑：找同一本书，chapterNum 比当前大 的最小那个
        LambdaQueryWrapper<BookChapter> nextQuery = new LambdaQueryWrapper<>();
        nextQuery.eq(BookChapter::getBookId, current.getBookId())
                .gt(BookChapter::getChapterNum, current.getChapterNum())
                .orderByAsc(BookChapter::getChapterNum)
                .last("limit 1")
                .select(BookChapter::getId);
        BookChapter next = chapterService.getOne(nextQuery);

        // 组装返回
        Map<String, Object> map = new HashMap<>();
        map.put("title", current.getTitle());
        map.put("content", current.getContent());
        map.put("prevId", prev != null ? prev.getId() : null);
        map.put("nextId", next != null ? next.getId() : null);

        return Result.success(map);
    }
}