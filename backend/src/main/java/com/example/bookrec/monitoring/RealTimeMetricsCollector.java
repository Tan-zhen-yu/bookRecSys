package com.example.bookrec.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 实时监控数据收集器
 */
@Aspect
@Component
@Slf4j
public class RealTimeMetricsCollector {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 计数器
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong errorRequests = new AtomicLong(0);
    private final AtomicLong bookDetailRequests = new AtomicLong(0);
    private final AtomicLong bookSellRequests = new AtomicLong(0);
    private final AtomicLong bookRankRequests = new AtomicLong(0);
    private final AtomicLong recommendRequests = new AtomicLong(0);

    /**
     * 监控API调用并收集指标
     */
    @Around("execution(* com.example.bookrec.controller.BookInfoController.getDetail(..))")
    public Object monitorBookDetail(ProceedingJoinPoint point) throws Throwable {
        return monitorApi(point, "book:detail", bookDetailRequests);
    }

    @Around("execution(* com.example.bookrec.controller.BookSellController.sellBookWithLock(..))")
    public Object monitorBookSell(ProceedingJoinPoint point) throws Throwable {
        return monitorApi(point, "book:sell", bookSellRequests);
    }

    @Around("execution(* com.example.bookrec.controller.BookInfoController.*Rank(..))")
    public Object monitorBookRank(ProceedingJoinPoint point) throws Throwable {
        return monitorApi(point, "book:rank", bookRankRequests);
    }

    @Around("execution(* com.example.bookrec.controller.RecommendResultController.*(..))")
    public Object monitorRecommend(ProceedingJoinPoint point) throws Throwable {
        return monitorApi(point, "recommend", recommendRequests);
    }

    @Around("execution(* com.example.bookrec.controller.TestController.*(..))")
    public Object monitorTest(ProceedingJoinPoint point) throws Throwable {
        return monitorApi(point, "test", recommendRequests);
    }

    private Object monitorApi(ProceedingJoinPoint point, String apiType, AtomicLong counter) throws Throwable {
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();
        counter.incrementAndGet();

        try {
            Object result = point.proceed();
            
            // 记录成功请求的响应时间
            long duration = System.currentTimeMillis() - startTime;
            recordResponseTime(duration);
            
            return result;
        } catch (Exception e) {
            errorRequests.incrementAndGet();
            log.error("API_ERROR: {} - {}", apiType, e.getMessage());
            throw e;
        }
    }

    /**
     * 记录响应时间
     */
    private void recordResponseTime(long duration) {
        // 记录到滑动窗口
        long currentMinute = System.currentTimeMillis() / (60 * 1000);
        String key = "metrics:response_time:" + currentMinute;
        
        // 使用Redis的INCRBY实现平均值计算
        redisTemplate.opsForValue().increment(key + ":total", duration);
        redisTemplate.opsForValue().increment(key + ":count", 1);
        redisTemplate.expire(key + ":total", 24, TimeUnit.HOURS);
        redisTemplate.expire(key + ":count", 24, TimeUnit.HOURS);
    }

    /**
     * 定时任务：每分钟同步计数器到Redis
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void syncCountersToRedis() {
        try {
            // 同步各种计数器
            redisTemplate.opsForValue().set("api:total", String.valueOf(totalRequests.get()));
            redisTemplate.opsForValue().set("api:error", String.valueOf(errorRequests.get()));
            redisTemplate.opsForValue().set("api:book:detail", String.valueOf(bookDetailRequests.get()));
            redisTemplate.opsForValue().set("api:book:sell", String.valueOf(bookSellRequests.get()));
            redisTemplate.opsForValue().set("api:book:rank", String.valueOf(bookRankRequests.get()));
            redisTemplate.opsForValue().set("api:recommend", String.valueOf(recommendRequests.get()));

            // 计算QPS（每秒请求数）
            long currentQps = calculateCurrentQPS();
            redisTemplate.opsForValue().set("api:current_qps", String.valueOf(currentQps));

            // 计算平均响应时间
            double avgResponseTime = calculateAvgResponseTime();
            redisTemplate.opsForValue().set("api:avg_response_time", String.valueOf(avgResponseTime));

            log.info("Metrics synced to Redis - QPS: {}, Avg Response Time: {}ms", currentQps, avgResponseTime);
        } catch (Exception e) {
            log.error("Failed to sync metrics to Redis", e);
        }
    }

    /**
     * 计算当前QPS
     */
    private long calculateCurrentQPS() {
        // 获取最近1分钟的请求数
        long currentMinute = System.currentTimeMillis() / (60 * 1000);
        long lastMinute = currentMinute - 1;
        
        String currentKey = "metrics:requests:" + currentMinute;
        String lastKey = "metrics:requests:" + lastMinute;
        
        long currentCount = getLongFromRedis(currentKey);
        long lastCount = getLongFromRedis(lastKey);
        
        return currentCount - lastCount;
    }

    /**
     * 计算平均响应时间
     */
    private double calculateAvgResponseTime() {
        long currentMinute = System.currentTimeMillis() / (60 * 1000);
        String totalKey = "metrics:response_time:" + currentMinute + ":total";
        String countKey = "metrics:response_time:" + currentMinute + ":count";
        
        long totalTime = getLongFromRedis(totalKey);
        long count = getLongFromRedis(countKey);
        
        return count > 0 ? (double) totalTime / count : 0.0;
    }

    private long getLongFromRedis(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }

    /**
     * 重置计数器（每天凌晨执行）
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行
    public void resetCounters() {
        totalRequests.set(0);
        errorRequests.set(0);
        bookDetailRequests.set(0);
        bookSellRequests.set(0);
        bookRankRequests.set(0);
        recommendRequests.set(0);
        
        log.info("Daily metrics counters reset");
    }
}
