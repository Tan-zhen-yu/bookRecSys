package com.example.bookrec.dto;

import com.example.bookrec.entity.UserBookRating;
import lombok.Data;

@Data
public class RatingVO extends UserBookRating {
    private String nickname;
    private String avatar;
}