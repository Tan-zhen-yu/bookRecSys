package com.example.bookrec.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("图书推荐系统接口文档")
                        .description("图书推荐系统 API 接口定义")
                        .version("1.0")
                        .build())
                // 分组名称
                .groupName("2.X版本")
                .select()
                // 这里指定 Controller 扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.example.bookrec.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}