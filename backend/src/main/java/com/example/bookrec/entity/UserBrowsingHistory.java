package com.example.bookrec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_browsing_history")
public class UserBrowsingHistory {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime createTime;
}