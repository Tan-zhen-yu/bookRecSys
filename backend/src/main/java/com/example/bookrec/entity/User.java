package com.example.bookrec.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
@Getter
@Setter
@TableName("sys_user")
@ApiModel(value = "User对象", description = "用户表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) // 必须加这个，否则 MP 会自动帮你生成雪花 ID
    private Long id;

    @ApiModelProperty("用户名")
    @TableField("username")
    private String username;

    @ApiModelProperty("密码")
    @TableField("password")
    private String password;

    @ApiModelProperty("昵称")
    @TableField("nickname")
    private String nickname;

    @ApiModelProperty("邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty("角色: 0普通用户, 1管理员")
    @TableField("role")
    private Integer role;

    @ApiModelProperty("兴趣标签(逗号分隔，用于冷启动)")
    @TableField("tags")
    private String tags;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty("逻辑删除: 0未删, 1已删")
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;
}
