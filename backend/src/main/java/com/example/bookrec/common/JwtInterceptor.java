package com.example.bookrec.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 先尝试从 Header 中获取 token
        String token = request.getHeader("token");

        // 2. 如果 Header 里没有，再去 Cookie 里找（兼容老逻辑）
        if (token == null || token.isEmpty()) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("auth_token".equals(cookie.getName())) {
                        token = cookie.getValue();
                    }
                }
            }
        }


        if (token == null || jwtUtils.validateToken(token).isEmpty()) {
            response.setStatus(401);
            return false;
        }

        return true;
    }
}