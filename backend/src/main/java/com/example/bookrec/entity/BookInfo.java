package com.example.bookrec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 图书信息表
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
@Getter
@Setter
@TableName("book_info")
@ApiModel(value = "BookInfo对象", description = "图书信息表")
public class BookInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("图书ID")
    @TableId("id")
    private Long id;

    @ApiModelProperty("所属分类ID")
    @TableField("category_id")
    private Long categoryId;

    @ApiModelProperty("书名")
    @TableField("title")
    private String title;

    @ApiModelProperty("作者")
    @TableField("author")
    private String author;

    @ApiModelProperty("出版社")
    @TableField("publisher")
    private String publisher;

    @ApiModelProperty("ISBN号")
    @TableField("isbn")
    private String isbn;

    @ApiModelProperty("出版日期")
    @TableField("publish_date")
    private LocalDate publishDate;

    @ApiModelProperty("价格")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty("封面图片URL")
    @TableField("cover_url")
    private String coverUrl;

    @ApiModelProperty("图书简介")
    @TableField("description")
    private String description;

    @ApiModelProperty("平均评分(冗余字段，方便排序)")
    @TableField("rating_avg")
    private BigDecimal ratingAvg;

    @ApiModelProperty("评分人数(冗余字段，方便筛选热门)")
    @TableField("rating_count")
    private Integer ratingCount;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted;

    @TableField(exist = false) // 告诉MP这个字段不在数据库表中
    private Integer matchScore;
}
