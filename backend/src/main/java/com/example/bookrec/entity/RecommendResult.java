package com.example.bookrec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 推荐结果缓存表
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
@Getter
@Setter
@TableName("recommend_result")
@ApiModel(value = "RecommendResult对象", description = "推荐结果缓存表")
public class RecommendResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    @TableId("user_id")
    private Long userId;

    @ApiModelProperty("推荐图书ID列表(逗号分隔，如: 101,102,103)")
    @TableField("book_ids")
    private String bookIds;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
