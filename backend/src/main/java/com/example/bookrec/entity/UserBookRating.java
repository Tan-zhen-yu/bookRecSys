package com.example.bookrec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户评分表(协同过滤核心数据)
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
@Getter
@Setter
@TableName("user_book_rating")
@ApiModel(value = "UserBookRating对象", description = "用户评分表(协同过滤核心数据)")
public class UserBookRating implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("主键")
    @TableId("id")
    private Long id;

    @ApiModelProperty("用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty("图书ID")
    @TableField("book_id")
    private Long bookId;

    @ApiModelProperty("评分(1.0-5.0)")
    @TableField("score")
    private BigDecimal score;

    @ApiModelProperty("评价内容")
    @TableField("comment")
    private String comment;

    @ApiModelProperty("评价时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
