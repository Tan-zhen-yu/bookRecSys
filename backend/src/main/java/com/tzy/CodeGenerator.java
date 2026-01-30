package com.tzy;

/**
 * Hello world!
 *
 */
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {
        // 1. 数据库配置
        String url = "jdbc:mysql://localhost:3306/book_rec_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root"; // 修改这里

        // 2. 项目路径 (修改为你的实际项目路径)
        // 例如: D:\\WorkSpace\\BookRecommendSystem\\src\\main\\java
        String projectPath = System.getProperty("user.dir");

        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author("Admin") // 作者
                            .enableSwagger() // 开启 swagger 模式 (可选)
                            .dateType(DateType.TIME_PACK)
                            .commentDate("yyyy-MM-dd")
                            .outputDir(projectPath + "/src/main/java"); // 输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.example.bookrec") // 修改你的包名
                            .entity("entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .mapper("mapper")
                            .xml("mapper.xml")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, projectPath + "/src/main/resources/mapper"));
                })
                .strategyConfig(builder -> {
                    builder.addInclude("sys_user", "book_info", "book_category", "user_book_rating", "user_book_shelf", "recommend_result") // 需要生成的表名
                            .addTablePrefix("sys_") // 过滤表前缀，生成 User 而不是 SysUser

                            // Entity 策略
                            .entityBuilder()
                            .enableLombok() // 开启 Lombok
                            .enableTableFieldAnnotation() // 开启字段注解
                            .logicDeleteColumnName("is_deleted") // 逻辑删除字段
                            .logicDeletePropertyName("isDeleted")

                            // Controller 策略
                            .controllerBuilder()
                            .enableRestStyle(); // 开启 @RestController
                })
                .templateEngine(new VelocityTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();

        System.out.println("代码生成完毕！");
    }
}