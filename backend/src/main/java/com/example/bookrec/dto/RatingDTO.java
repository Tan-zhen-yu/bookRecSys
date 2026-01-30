package com.example.bookrec.dto;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RatingDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Long userId;
    private String nickname; // 用户昵称
    private Long bookId;
    private String bookTitle; // 书名
    private String bookCover; // 封面
    private Double score;
    private String comment;
    private LocalDateTime createTime;
}