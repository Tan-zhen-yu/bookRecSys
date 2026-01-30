package com.example.bookrec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("book_chapter")
public class BookChapter {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long bookId;
    private Integer chapterNum;
    private String title;
    private String content; // 正文
    private LocalDateTime createTime;
}