package com.example.bookrec.service;

import com.example.bookrec.entity.UserBookRating;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户评分表(协同过滤核心数据) 服务类
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
public interface IUserBookRatingService extends IService<UserBookRating> {
    boolean rateBook(UserBookRating rating); // 评分业务逻辑
}
