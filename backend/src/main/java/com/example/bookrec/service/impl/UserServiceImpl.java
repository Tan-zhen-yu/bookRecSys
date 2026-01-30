package com.example.bookrec.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookrec.entity.User;
import com.example.bookrec.mapper.UserMapper;
import com.example.bookrec.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public User login(User user) {
        // 1. 根据用户名查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User dbUser = getOne(wrapper);

        if (dbUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 校验密码 (这里使用了Hutool的MD5加密，防止数据库存明文)
        // 假设前端传来的也是明文，我们加密后比对
        String inputPass = DigestUtil.md5Hex(user.getPassword());
        if (!dbUser.getPassword().equals(inputPass)) {
            throw new RuntimeException("密码错误");
        }

        // 3. 登录成功，清空密码防止泄露
        dbUser.setPassword(null);
        return dbUser;
    }

    @Override
    public User register(User user) {
        // 1. 校验用户名是否重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 密码加密存储
        if (StrUtil.isBlank(user.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }
        user.setPassword(DigestUtil.md5Hex(user.getPassword()));

        // 3. 设置默认值
        if (StrUtil.isBlank(user.getNickname())) {
            user.setNickname("书友" + System.currentTimeMillis());
        }
        user.setRole(0); // 默认普通用户

        save(user);
        return user;
    }
}