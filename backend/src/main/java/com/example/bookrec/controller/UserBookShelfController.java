package com.example.bookrec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookrec.common.Result;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.entity.UserBookShelf;
import com.example.bookrec.service.IBookInfoService;
import com.example.bookrec.service.IUserBookShelfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户书架/收藏表 前端控制器
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
@RestController
@RequestMapping("/shelf")
public class UserBookShelfController {

    @Autowired
    private IUserBookShelfService shelfService;
    @Autowired
    private IBookInfoService bookInfoService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // 注入 Redis

    // 添加收藏
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody UserBookShelf shelf) {
        try {
            boolean success = shelfService.save(shelf);
            if (success) {
                String cacheKey = "rec:user:" + shelf.getUserId();
                redisTemplate.delete(cacheKey);
                System.out.println("用户修改标签，已清除缓存: " + cacheKey);
            }
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("已在书架中");
        }
    }

    // 取消收藏
    @DeleteMapping("/remove")
    public Result<Boolean> remove(@RequestParam Long userId, @RequestParam Long bookId) {
        LambdaQueryWrapper<UserBookShelf> query = new LambdaQueryWrapper<>();
        query.eq(UserBookShelf::getUserId, userId).eq(UserBookShelf::getBookId, bookId);
        return Result.success(shelfService.remove(query));
    }

    // 检查是否收藏
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Long userId, @RequestParam Long bookId) {
        LambdaQueryWrapper<UserBookShelf> query = new LambdaQueryWrapper<>();
        query.eq(UserBookShelf::getUserId, userId).eq(UserBookShelf::getBookId, bookId);
        return Result.success(shelfService.count(query) > 0);
    }

    // 我的书架列表
    @GetMapping("/my")
    public Result<List<BookInfo>> myShelf(@RequestParam Long userId) {
        // 1. 查关联表
        LambdaQueryWrapper<UserBookShelf> query = new LambdaQueryWrapper<>();
        query.eq(UserBookShelf::getUserId, userId).orderByDesc(UserBookShelf::getCreateTime);
        List<UserBookShelf> shelves = shelfService.list(query);

        if (shelves.isEmpty()) return Result.success(new ArrayList<>());

        // 2. 查图书详情
        List<Long> bookIds = shelves.stream().map(UserBookShelf::getBookId).collect(Collectors.toList());
        return Result.success(bookInfoService.listByIds(bookIds));
    }
}