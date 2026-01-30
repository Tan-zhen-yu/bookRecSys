package com.example.bookrec.service;

import com.example.bookrec.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
public interface IUserService extends IService<User> {
    User login(User user); // 登录

    User register(User user); // 注册
}
