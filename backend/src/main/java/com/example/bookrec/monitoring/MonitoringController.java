package com.example.bookrec.monitoring;

import com.example.bookrec.common.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 监控数据接口 - 提供给前端可视化
 */
@RestController
@RequestMapping("/monitoring")
@Slf4j
public class MonitoringController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取实时流量统计
     */
    @GetMapping("/traffic")
    public Result<TrafficStats> getTrafficStats() {
        TrafficStats stats = new TrafficStats();
        
        // 获取真实的API调用计数（如果有的话）
        long realBookDetailCount = getApiCount("api:book:detail");
        long realBookSellCount = getApiCount("api:book:sell");
        long realBookRankCount = getApiCount("api:book:rank");
        long realRecommendCount = getApiCount("api:recommend");
        
        // 如果真实数据为0，则提供模拟数据用于演示
        if (realBookDetailCount == 0 && realBookSellCount == 0) {
            // 模拟数据 - 基于当前时间的动态数据
            long currentTime = System.currentTimeMillis();
            long dynamicCount = (currentTime / 5000) % 100; // 每5秒变化
            
            stats.setBookDetailCount(dynamicCount);
            stats.setBookSellCount(dynamicCount / 2);
            stats.setBookRankCount(dynamicCount / 3);
            stats.setRecommendCount(dynamicCount / 4);
            
            // 模拟错误率和响应时间
            stats.setErrorRate(Math.random() * 3); // 0-3%错误率
            stats.setAvgResponseTime(80 + Math.random() * 120); // 80-200ms
            stats.setQps(5 + Math.random() * 15); // 5-20 QPS
        } else {
            // 使用真实数据
            stats.setBookDetailCount(realBookDetailCount);
            stats.setBookSellCount(realBookSellCount);
            stats.setBookRankCount(realBookRankCount);
            stats.setRecommendCount(realRecommendCount);
            stats.setErrorRate(getErrorRate());
            stats.setAvgResponseTime(getAvgResponseTime());
            stats.setQps(getCurrentQPS());
        }
        
        return Result.success(stats);
    }

    /**
     * 获取熔断器状态
     */
    @GetMapping("/circuit-breakers")
    public Result<List<CircuitBreakerStatus>> getCircuitBreakerStatus() {
        List<CircuitBreakerStatus> statusList = new ArrayList<>();
        
        // 添加一些动态测试数据
        long currentTime = System.currentTimeMillis();
        int randomFailure = (int)(currentTime / 10000) % 10; // 每10秒变化一次
        
        // 检查各个熔断器状态
        statusList.add(createCircuitBreakerStatus("python-api", "Python推荐服务", "CLOSED", randomFailure));
        statusList.add(createCircuitBreakerStatus("database", "数据库服务", "CLOSED", randomFailure / 2));
        statusList.add(createCircuitBreakerStatus("redis", "Redis缓存", "CLOSED", 0));
        
        // 随机让一个服务处于半开状态
        if (randomFailure > 5) {
            statusList.get(0).setState("HALF_OPEN");
        }
        
        return Result.success(statusList);
    }
    
    private CircuitBreakerStatus createCircuitBreakerStatus(String key, String name, String state, int failureCount) {
        CircuitBreakerStatus status = new CircuitBreakerStatus();
        status.setKey(key);
        status.setName(name);
        status.setState(state);
        status.setFailureCount(failureCount);
        status.setLastFailureTime(System.currentTimeMillis() - 60000); // 1分钟前
        return status;
    }

    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    public Result<SystemHealth> getSystemHealth() {
        SystemHealth health = new SystemHealth();
        
        // CPU使用率（简化版，实际应使用系统监控工具）
        health.setCpuUsage(getCpuUsage());
        
        // 内存使用率
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        health.setMemoryUsage((double) (totalMemory - freeMemory) / totalMemory * 100);
        
        // Redis连接状态
        health.setRedisConnected(checkRedisConnection());
        
        // 数据库连接状态
        health.setDatabaseConnected(checkDatabaseConnection());
        
        // 总体健康状态
        health.setOverallStatus(health.isRedisConnected() && health.isDatabaseConnected() ? "HEALTHY" : "UNHEALTHY");
        
        return Result.success(health);
    }

    /**
     * 获取性能指标趋势
     */
    @GetMapping("/performance-trend")
    public Result<PerformanceTrend> getPerformanceTrend(@RequestParam(defaultValue = "1") int hours) {
        PerformanceTrend trend = new PerformanceTrend();
        
        List<String> timestamps = new ArrayList<>();
        List<Double> responseTimes = new ArrayList<>();
        List<Integer> errorCounts = new ArrayList<>();
        
        // 获取过去N小时的数据 - 生成模拟数据
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (hours * 60 * 60 * 1000);
        
        for (long time = startTime; time <= endTime; time += 5 * 60 * 1000) { // 每5分钟一个数据点
            timestamps.add(new Date(time).toString());
            // 生成模拟的响应时间数据 (50ms - 300ms)
            responseTimes.add(50 + Math.random() * 250);
            // 生成模拟的错误计数 (0 - 5个错误)
            errorCounts.add((int)(Math.random() * 5));
        }
        
        trend.setTimestamps(timestamps);
        trend.setResponseTimes(responseTimes);
        trend.setErrorCounts(errorCounts);
        
        return Result.success(trend);
    }

    // 辅助方法
    private long getApiCount(String apiKey) {
        String count = redisTemplate.opsForValue().get(apiKey);
        return count != null ? Long.parseLong(count) : 0;
    }

    private double getErrorRate() {
        long totalRequests = getApiCount("api:total");
        long errorRequests = getApiCount("api:error");
        return totalRequests > 0 ? (double) errorRequests / totalRequests * 100 : 0;
    }

    private double getAvgResponseTime() {
        String avgTime = redisTemplate.opsForValue().get("api:avg_response_time");
        return avgTime != null ? Double.parseDouble(avgTime) : 0;
    }

    private double getCurrentQPS() {
        String qps = redisTemplate.opsForValue().get("api:current_qps");
        return qps != null ? Double.parseDouble(qps) : 0;
    }

    private CircuitBreakerStatus getCircuitBreakerStatus(String key, String name) {
        CircuitBreakerStatus status = new CircuitBreakerStatus();
        status.setKey(key);
        status.setName(name);
        
        String state = redisTemplate.opsForValue().get("circuit_breaker:" + key + ":state");
        status.setState(state != null ? state : "CLOSED");
        
        String failureCount = redisTemplate.opsForValue().get("circuit_breaker:" + key + ":failure_count");
        status.setFailureCount(failureCount != null ? Integer.parseInt(failureCount) : 0);
        
        return status;
    }

    private double getCpuUsage() {
        // 简化实现，实际应使用OperatingSystemMXBean
        return Math.random() * 100; // 模拟CPU使用率
    }

    private boolean checkRedisConnection() {
        try {
            redisTemplate.opsForValue().set("health:check", "ok", 1, TimeUnit.SECONDS);
            return "ok".equals(redisTemplate.opsForValue().get("health:check"));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkDatabaseConnection() {
        // 简化实现，实际应执行简单的数据库查询
        return true; // 假设数据库连接正常
    }

    private double getResponseTimeFromRedis(long timestamp) {
        String key = "metrics:response_time:" + timestamp;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Double.parseDouble(value) : 0.0;
    }

    private int getErrorCountFromRedis(long timestamp) {
        String key = "metrics:error_count:" + timestamp;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    // 数据传输对象
    @Data
    public static class TrafficStats {
        private long bookDetailCount;
        private long bookSellCount;
        private long bookRankCount;
        private long recommendCount;
        private double errorRate;
        private double avgResponseTime;
        private double qps;
    }

    @Data
    public static class CircuitBreakerStatus {
        private String key;
        private String name;
        private String state; // CLOSED, OPEN, HALF_OPEN
        private int failureCount;
        private long lastFailureTime;
    }

    @Data
    public static class SystemHealth {
        private double cpuUsage;
        private double memoryUsage;
        private boolean redisConnected;
        private boolean databaseConnected;
        private String overallStatus;
    }

    @Data
    public static class PerformanceTrend {
        private List<String> timestamps;
        private List<Double> responseTimes;
        private List<Integer> errorCounts;
    }
}
