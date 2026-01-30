package com.example.bookrec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookrec.common.Result;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.entity.UserBrowsingHistory;
import com.example.bookrec.service.IBookInfoService;
import com.example.bookrec.service.IUserBrowsingHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private IUserBrowsingHistoryService historyService;
    @Autowired
    private IBookInfoService bookInfoService;

    // 1. 添加/更新浏览历史
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody UserBrowsingHistory history) {
        // 先查询是否存在
        LambdaQueryWrapper<UserBrowsingHistory> query = new LambdaQueryWrapper<>();
        query.eq(UserBrowsingHistory::getUserId, history.getUserId())
                .eq(UserBrowsingHistory::getBookId, history.getBookId());
        UserBrowsingHistory exist = historyService.getOne(query);

        if (exist != null) {
            exist.setCreateTime(LocalDateTime.now());
            historyService.updateById(exist);
        } else {
            history.setCreateTime(LocalDateTime.now());
            historyService.save(history);
        }
        return Result.success(true);
    }

    // 2. 获取我的浏览历史 (返回图书列表)
    @GetMapping("/my")
    public Result<List<BookInfo>> myHistory(@RequestParam Long userId) {
        // 查最新的 20 条
        LambdaQueryWrapper<UserBrowsingHistory> query = new LambdaQueryWrapper<>();
        query.eq(UserBrowsingHistory::getUserId, userId)
                .orderByDesc(UserBrowsingHistory::getCreateTime)
                .last("limit 20");
        List<UserBrowsingHistory> histories = historyService.list(query);

        if (histories.isEmpty()) return Result.success(new ArrayList<>());

        List<Long> bookIds = histories.stream().map(UserBrowsingHistory::getBookId).collect(Collectors.toList());

        // 这里的 listByIds 可能会乱序，为了保持浏览顺序，我们手动重排一下 (可选)
        List<BookInfo> books = bookInfoService.listByIds(bookIds);

        // 简单返回即可
        return Result.success(books);
    }
}