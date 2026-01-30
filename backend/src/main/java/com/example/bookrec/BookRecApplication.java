package com.example.bookrec;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类
 */
@SpringBootApplication
@MapperScan("com.example.bookrec.mapper") // 关键：扫描你代码生成器产出的 Mapper 接口
public class BookRecApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookRecApplication.class, args);
        System.out.println("==================================================");
        System.out.println("   图书推荐系统启动成功！");
        System.out.println("   Knife4j 接口文档地址: http://localhost:8080/doc.html");
        System.out.println("==================================================");
    }
}