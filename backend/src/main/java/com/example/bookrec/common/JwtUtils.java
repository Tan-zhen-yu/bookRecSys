package com.example.bookrec.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    private static final String SECRET_KEY = "MySuperSecretKey_ChangeThisInProduction"; // 密钥
    private static final long EXPIRATION_TIME = 86400000; // 24小时 (毫秒)

    // 生成 Token
    public String createToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // 可以存入自定义信息
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 校验 Token (将来在过滤器里用)
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null; // 校验失败
        }
    }
}