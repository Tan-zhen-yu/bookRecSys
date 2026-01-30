package com.example.bookrec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 图书分类表
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
@Getter
@Setter
@TableName("book_category")
@ApiModel(value = "BookCategory对象", description = "图书分类表")
public class BookCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("分类ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("分类名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("父分类ID(0为顶级)")
    @TableField("parent_id")
    private Long parentId;
}
