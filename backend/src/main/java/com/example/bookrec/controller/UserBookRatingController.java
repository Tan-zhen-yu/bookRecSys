package com.example.bookrec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookrec.common.Result;
import com.example.bookrec.dto.RatingDTO;
import com.example.bookrec.dto.RatingVO;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.entity.User;
import com.example.bookrec.entity.UserBookRating;
import com.example.bookrec.service.IBookInfoService;
import com.example.bookrec.service.IUserBookRatingService;
import com.example.bookrec.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rating")
public class UserBookRatingController {

    @Autowired
    private IUserBookRatingService ratingService;

    @Autowired
    private IUserService userService;
    @Autowired
    private IBookInfoService bookInfoService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // 注入 Redis

    // 提交评分 POST /rating/add
    @PostMapping("/add")
    public Result<Boolean> addRating(@RequestBody UserBookRating rating) {
        if (rating.getUserId() == null || rating.getBookId() == null) {
            return Result.error("参数错误");
        }
        boolean success = ratingService.rateBook(rating);
        if (success) {
            String cacheKey = "rec:user:" + rating.getUserId();
            redisTemplate.delete(cacheKey);
            System.out.println("用户提交评分，已清除缓存: " + cacheKey);
        }
        return Result.success(success);
    }

    // 获取某本书的评论列表 GET /rating/list?bookId=1
    @GetMapping("/list")
    public Result<List<RatingVO>> listComments(@RequestParam Long bookId) {
        // 1. MP 原有逻辑：查出该书的所有评价
        LambdaQueryWrapper<UserBookRating> query = new LambdaQueryWrapper<>();
        query.eq(UserBookRating::getBookId, bookId).orderByDesc(UserBookRating::getCreateTime);
        List<UserBookRating> ratings = ratingService.list(query);

        if (ratings.isEmpty()) return Result.success(new ArrayList<>());

        // 2. 提取所有评价者的 userId (去重)
        List<Long> userIds = ratings.stream()
                .map(UserBookRating::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 批量查出用户信息，转成 Map<Id, User> 方便后续匹配
        // 假设你的用户 Service 叫 userService
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 4. 组装 VO 列表
        List<RatingVO> voList = ratings.stream().map(rating -> {
            RatingVO vo = new RatingVO();
            BeanUtils.copyProperties(rating, vo); // 复制基础字段

            // 匹配用户信息
            User user = userMap.get(rating.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());

            } else {
                vo.setNickname("神秘书友"); // 兜底：万一用户被删了
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    // UserBookRatingController.java 添加：

    // 管理员获取所有评论
    @GetMapping("/listAll")
    public Result<List<UserBookRating>> listAllRatings() {
        // 实际应该分页，这里为了省事查全部
        LambdaQueryWrapper<UserBookRating> query = new LambdaQueryWrapper<>();
        query.orderByDesc(UserBookRating::getCreateTime);
        return Result.success(ratingService.list(query));
    }

    // 删除评论
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRating(@PathVariable Long id) {
        return Result.success(ratingService.removeById(id));
    }

    // UserBookRatingController.java



    // 广场最新动态
    @GetMapping("/square")
    public Result<List<RatingDTO>> getSquareList() {
        // 查最新的 20 条评论
        LambdaQueryWrapper<UserBookRating> query = new LambdaQueryWrapper<>();
        query.orderByDesc(UserBookRating::getCreateTime)
                .isNotNull(UserBookRating::getComment) // 必须有文字评论
                .ne(UserBookRating::getComment, "")
                .last("limit 20");
        List<UserBookRating> list = ratingService.list(query);

        // 转换成 DTO
        List<RatingDTO> dtos = new ArrayList<>();
        for (UserBookRating r : list) {
            RatingDTO dto = new RatingDTO();
            // 复制基础属性
            dto.setId(r.getId());
            dto.setScore(r.getScore().doubleValue());
            dto.setComment(r.getComment());
            dto.setCreateTime(r.getCreateTime());
            dto.setUserId(r.getUserId());
            dto.setBookId(r.getBookId());

            // 查人名
            User user = userService.getById(r.getUserId());
            if (user != null) dto.setNickname(user.getNickname());

            // 查书名
            BookInfo book = bookInfoService.getById(r.getBookId());
            if (book != null) {
                dto.setBookTitle(book.getTitle());
                dto.setBookCover(book.getCoverUrl());
            }

            dtos.add(dto);
        }
        return Result.success(dtos);
    }

    @GetMapping("/my")
    public Result<List<RatingDTO>> getMyRatings(@RequestParam Long userId) {
        LambdaQueryWrapper<UserBookRating> query = new LambdaQueryWrapper<>();
        query.eq(UserBookRating::getUserId, userId)
                .orderByDesc(UserBookRating::getCreateTime);
        List<UserBookRating> list = ratingService.list(query);

        List<RatingDTO> dtos = new ArrayList<>();
        for (UserBookRating r : list) {
            RatingDTO dto = new RatingDTO();
            dto.setId(r.getId());
            dto.setScore(r.getScore().doubleValue());
            dto.setComment(r.getComment());
            dto.setCreateTime(r.getCreateTime());
            dto.setBookId(r.getBookId());

            // 填充图书信息
            BookInfo book = bookInfoService.getById(r.getBookId());
            if (book != null) {
                dto.setBookTitle(book.getTitle());
                dto.setBookCover(book.getCoverUrl());
            }
            dtos.add(dto);
        }
        return Result.success(dtos);
    }
}