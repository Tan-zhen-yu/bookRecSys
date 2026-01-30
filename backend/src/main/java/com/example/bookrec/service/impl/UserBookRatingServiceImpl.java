package com.example.bookrec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.entity.UserBookRating;
import com.example.bookrec.mapper.UserBookRatingMapper;
import com.example.bookrec.service.IBookInfoService;
import com.example.bookrec.service.IUserBookRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class UserBookRatingServiceImpl extends ServiceImpl<UserBookRatingMapper, UserBookRating> implements IUserBookRatingService {

    @Autowired
    private IBookInfoService bookInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class) // 事务控制，保证同步
    public boolean rateBook(UserBookRating rating) {
        // 1. 检查该用户是否已经对这本书评过分（避免刷分）
        LambdaQueryWrapper<UserBookRating> query = new LambdaQueryWrapper<>();
        query.eq(UserBookRating::getUserId, rating.getUserId())
                .eq(UserBookRating::getBookId, rating.getBookId());

        if (count(query) > 0) {
            throw new RuntimeException("您已经评价过这本书了");
        }

        // 2. 保存新的评分
        save(rating);

        // 3. 计算该书的最新平均分和评分人数
        // SQL: SELECT AVG(score) as avgScore, COUNT(*) as count FROM user_book_rating WHERE book_id = ?
        QueryWrapper<UserBookRating> statQuery = new QueryWrapper<>();
        statQuery.select("AVG(score) as avgScore", "COUNT(*) as count")
                .eq("book_id", rating.getBookId());

        Map<String, Object> map = this.getMap(statQuery);

        double avgScore = ((Number) map.get("avgScore")).doubleValue();
        int count = ((Number) map.get("count")).intValue();

        // 4. 更新图书信息表 (冗余字段，方便查询)
        BookInfo bookInfo = bookInfoService.getById(rating.getBookId());
        bookInfo.setRatingAvg(BigDecimal.valueOf(avgScore));
        bookInfo.setRatingCount(count);
        bookInfoService.updateById(bookInfo);

        return true;
    }
}