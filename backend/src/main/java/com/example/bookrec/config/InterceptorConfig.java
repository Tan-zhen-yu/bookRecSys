package com.example.bookrec.config;

import com.example.bookrec.common.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")    // 拦截所有路径
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/doc.html",                // Knife4j 文档地址
                        "/webjars/**",              // 静态资源
                        "/swagger-resources/**",    // Swagger 资源
                        "/v2/api-docs/**",          // Swagger 2 接口文档数据
                        "/v3/api-docs/**",          // Swagger 3 接口文档数据
                        "/favicon.ico"              // 图标（可选）
                );
    }
}