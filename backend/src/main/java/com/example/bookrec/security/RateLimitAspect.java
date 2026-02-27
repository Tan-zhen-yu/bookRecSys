package com.example.bookrec.security;

import com.example.bookrec.common.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流切面 - 工业级防护
 */
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 限流注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface RateLimiter {
        // 限流key
        String key() default "";
        // 限流次数
        int count() default 100;
        // 限流时间窗口（秒）
        int period() default 60;
        // 限流类型
        LimitType limitType() default LimitType.DEFAULT;
    }

    public enum LimitType {
        DEFAULT,    // 默认策略（全局限流）
        IP,         // IP限流
        USER        // 用户限流
    }

    @Around("@annotation(rateLimiter)")
    public Object doLimit(ProceedingJoinPoint point, RateLimiter rateLimiter) throws Throwable {
        String key = getCombineKey(rateLimiter, point);
        
        try {
            // 获取当前计数
            String countStr = redisTemplate.opsForValue().get(key);
            int count = countStr == null ? 0 : Integer.parseInt(countStr);
            
            if (count >= rateLimiter.count()) {
                // 超过限流次数
                return Result.error("请求过于频繁，请稍后再试");
            }
            
            // 增加计数
            if (count == 0) {
                // 第一次访问，设置过期时间
                redisTemplate.opsForValue().set(key, "1", rateLimiter.period(), TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().increment(key);
            }
            
            return point.proceed();
        } catch (Exception e) {
            // 限流异常不影响主流程
            return point.proceed();
        }
    }

    private String getCombineKey(RateLimiter rateLimiter, ProceedingJoinPoint point) {
        StringBuilder key = new StringBuilder(rateLimiter.key());
        
        if (rateLimiter.limitType() == LimitType.IP) {
            // IP限流（实际应从request获取IP）
            key.append(":IP:127.0.0.1");
        } else if (rateLimiter.limitType() == LimitType.USER) {
            // 用户限流（实际应从JWT获取用户ID）
            key.append(":USER:1");
        }
        
        // 添加方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        key.append(":").append(signature.getMethod().getName());
        
        return key.toString();
    }
}
