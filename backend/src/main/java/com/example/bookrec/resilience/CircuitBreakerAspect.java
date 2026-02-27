package com.example.bookrec.resilience;

import com.example.bookrec.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 智能熔断器 - 工业级容错保护
 * 
 * 功能：
 * 1. 自动检测接口异常
 * 2. 达到阈值自动熔断
 * 3. 半开状态尝试恢复
 * 4. 全自动恢复机制
 */
@Aspect
@Component
@Slf4j
public class CircuitBreakerAspect {

    private final StringRedisTemplate redisTemplate;

    public CircuitBreakerAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 熔断器注解 - 可配置熔断策略
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface CircuitBreaker {
        // 熔断器标识
        String key() default "";
        // 失败阈值（连续失败多少次触发熔断）
        int failureThreshold() default 5;
        // 熔断持续时间（秒）
        int timeout() default 60;
        // 半开状态最大尝试次数
        int halfOpenMaxCalls() default 3;
        // 成功率阈值（半开状态时，成功率超过此值则恢复）
        double successThreshold() default 0.5;
        // 最小请求数（请求数少于此值时不触发熔断）
        int minRequestThreshold() default 10;
    }

    @Around("@annotation(circuitBreaker)")
    public Object doCircuitBreaker(ProceedingJoinPoint point, CircuitBreaker circuitBreaker) throws Throwable {
        String key = "circuit_breaker:" + circuitBreaker.key();
        
        // 获取熔断器状态
        CircuitBreakerState state = getCircuitBreakerState(key);
        
        // 记录请求总数
        incrementRequestCount(key);
        
        if (state.isOpen()) {
            // 熔断开启 - 直接返回降级响应
            log.warn("🔥 CIRCUIT_BREAKER_OPEN: {} 熔断器开启，直接降级", key);
            return createFallbackResponse(point, circuitBreaker);
        }
        
        if (state.isHalfOpen()) {
            // 半开状态 - 限制尝试次数
            long halfOpenCalls = getHalfOpenCallCount(key);
            if (halfOpenCalls >= circuitBreaker.halfOpenMaxCalls()) {
                log.warn("⚠️ CIRCUIT_BREAKER_HALF_OPEN_LIMIT: {} 半开状态尝试次数已达上限", key);
                return createFallbackResponse(point, circuitBreaker);
            }
            incrementHalfOpenCallCount(key);
        }
        
        try {
            // 执行目标方法
            long startTime = System.currentTimeMillis();
            Object result = point.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录成功请求
            recordSuccess(key, duration);
            
            // 检查是否可以恢复
            if (state.isHalfOpen() && canRecover(key, circuitBreaker)) {
                closeCircuitBreaker(key);
                log.info("✅ CIRCUIT_BREAKER_RECOVERED: {} 熔断器已恢复", key);
            }
            
            return result;
            
        } catch (Exception e) {
            // 记录失败请求
            recordFailure(key);
            
            int failureCount = getFailureCount(key);
            long totalRequests = getTotalRequestCount(key);
            
            // 检查是否需要开启熔断器
            if (shouldOpenCircuitBreaker(failureCount, totalRequests, circuitBreaker)) {
                openCircuitBreaker(key, circuitBreaker.timeout());
                log.error("🚨 CIRCUIT_BREAKER_OPENED: {} 失败次数达到阈值 {}/{}，熔断器开启", 
                    key, failureCount, circuitBreaker.failureThreshold());
            }
            
            throw e;
        }
    }

    /**
     * 判断是否应该开启熔断器
     */
    private boolean shouldOpenCircuitBreaker(int failureCount, long totalRequests, CircuitBreaker config) {
        // 请求数太少不触发熔断
        if (totalRequests < config.minRequestThreshold()) {
            return false;
        }
        
        // 失败次数达到阈值
        return failureCount >= config.failureThreshold();
    }

    /**
     * 判断是否可以恢复
     */
    private boolean canRecover(String key, CircuitBreaker config) {
        long successCount = getSuccessCount(key);
        long totalHalfOpenCalls = getHalfOpenCallCount(key);
        
        if (totalHalfOpenCalls == 0) return false;
        
        double successRate = (double) successCount / totalHalfOpenCalls;
        return successRate >= config.successThreshold();
    }

    /**
     * 创建降级响应
     */
    private Object createFallbackResponse(ProceedingJoinPoint point, CircuitBreaker config) {
        String methodName = point.getSignature().getName();
        String className = point.getTarget().getClass().getSimpleName();
        
        log.info("🛡️ FALLBACK_TRIGGERED: {}.{} - 服务暂时不可用", className, methodName);
        
        // 根据方法返回类型创建不同的降级响应
        Class<?> returnType = point.getSignature().getDeclaringType();
        
        if (methodName.contains("getDetail") || methodName.contains("list")) {
            // 查询类接口返回空结果
            return Result.success("服务暂时不可用，请稍后再试");
        } else if (methodName.contains("sell") || methodName.contains("borrow")) {
            // 操作类接口返回错误
            return Result.error("系统繁忙，请稍后再试");
        } else {
            // 默认降级响应
            return Result.error("服务暂时不可用");
        }
    }

    // ============ Redis操作方法 ============

    private CircuitBreakerState getCircuitBreakerState(String key) {
        String state = redisTemplate.opsForValue().get(key + ":state");
        if ("OPEN".equals(state)) {
            return CircuitBreakerState.OPEN;
        } else if ("HALF_OPEN".equals(state)) {
            return CircuitBreakerState.HALF_OPEN;
        }
        return CircuitBreakerState.CLOSED;
    }

    private void openCircuitBreaker(String key, int timeout) {
        redisTemplate.opsForValue().set(key + ":state", "OPEN", timeout, TimeUnit.SECONDS);
        // 重置计数器
        redisTemplate.delete(key + ":half_open_calls");
        redisTemplate.delete(key + ":success_count");
    }

    private void closeCircuitBreaker(String key) {
        redisTemplate.delete(key + ":state");
        resetCounters(key);
    }

    private void resetCounters(String key) {
        redisTemplate.delete(key + ":failure_count");
        redisTemplate.delete(key + ":success_count");
        redisTemplate.delete(key + ":request_count");
        redisTemplate.delete(key + ":half_open_calls");
    }

    private void recordFailure(String key) {
        redisTemplate.opsForValue().increment(key + ":failure_count");
        redisTemplate.expire(key + ":failure_count", 24, TimeUnit.HOURS);
    }

    private void recordSuccess(String key, long duration) {
        redisTemplate.opsForValue().increment(key + ":success_count");
        redisTemplate.opsForValue().increment(key + ":request_count");
        
        // 记录响应时间用于监控
        long currentMinute = System.currentTimeMillis() / (60 * 1000);
        String responseTimeKey = "metrics:response_time:" + currentMinute;
        redisTemplate.opsForValue().increment(responseTimeKey + ":total", duration);
        redisTemplate.opsForValue().increment(responseTimeKey + ":count");
        
        redisTemplate.expire(key + ":success_count", 24, TimeUnit.HOURS);
        redisTemplate.expire(key + ":request_count", 24, TimeUnit.HOURS);
        redisTemplate.expire(responseTimeKey + ":total", 24, TimeUnit.HOURS);
        redisTemplate.expire(responseTimeKey + ":count", 24, TimeUnit.HOURS);
    }

    private void incrementRequestCount(String key) {
        redisTemplate.opsForValue().increment(key + ":request_count");
        redisTemplate.expire(key + ":request_count", 24, TimeUnit.HOURS);
    }

    private void incrementHalfOpenCallCount(String key) {
        redisTemplate.opsForValue().increment(key + ":half_open_calls");
        redisTemplate.expire(key + ":half_open_calls", 24, TimeUnit.HOURS);
    }

    private int getFailureCount(String key) {
        String value = redisTemplate.opsForValue().get(key + ":failure_count");
        return value != null ? Integer.parseInt(value) : 0;
    }

    private long getSuccessCount(String key) {
        String value = redisTemplate.opsForValue().get(key + ":success_count");
        return value != null ? Long.parseLong(value) : 0;
    }

    private long getTotalRequestCount(String key) {
        String value = redisTemplate.opsForValue().get(key + ":request_count");
        return value != null ? Long.parseLong(value) : 0;
    }

    private long getHalfOpenCallCount(String key) {
        String value = redisTemplate.opsForValue().get(key + ":half_open_calls");
        return value != null ? Long.parseLong(value) : 0;
    }

    /**
     * 熔断器状态枚举
     */
    public enum CircuitBreakerState {
        CLOSED,    // 关闭状态（正常）
        OPEN,       // 开启状态（熔断）
        HALF_OPEN;  // 半开状态（尝试恢复）
        
        public boolean isOpen() {
            return this == OPEN;
        }
        
        public boolean isHalfOpen() {
            return this == HALF_OPEN;
        }
    }
}
