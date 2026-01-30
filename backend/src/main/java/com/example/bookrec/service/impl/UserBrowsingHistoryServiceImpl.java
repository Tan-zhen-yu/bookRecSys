package com.example.bookrec.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookrec.entity.UserBrowsingHistory;
import com.example.bookrec.mapper.UserBrowsingHistoryMapper;
import com.example.bookrec.service.IUserBrowsingHistoryService;
import org.springframework.stereotype.Service;

@Service
public class UserBrowsingHistoryServiceImpl extends ServiceImpl<UserBrowsingHistoryMapper, UserBrowsingHistory> implements IUserBrowsingHistoryService {}