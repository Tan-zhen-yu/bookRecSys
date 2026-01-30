package com.example.bookrec.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace(); // 打印控制台，方便调试
        return Result.error(e.getMessage() != null ? e.getMessage() : "系统发生未知错误");
    }
}