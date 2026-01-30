package com.example.bookrec.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 当访问 http://localhost:8080/files/** 时
        // 实际上是去访问你电脑的 D:/book_covers/ 目录
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:D:\\Soft\\bookrec\\book-rec-frontend\\images");
    }
}