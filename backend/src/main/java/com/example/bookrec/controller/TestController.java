package com.example.bookrec.controller;

import com.example.bookrec.common.Result;
import com.example.bookrec.resilience.CircuitBreakerAspect.CircuitBreaker;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

/**
 * 测试控制器 - 用于测试熔断器功能
 */
@RestController
@RequestMapping("/test")
public class TestController {

    private final Random random = new Random();
    private int failureCount = 0;

    /**
     * 测试熔断器 - 模拟不稳定接口
     * 30%概率失败，用于测试熔断器触发
     */
    @GetMapping("/unstable")
    @CircuitBreaker(
        key = "test_unstable", 
        failureThreshold = 5, 
        timeout = 30,
        minRequestThreshold = 3,
        successThreshold = 0.5
    )
    public Result<String> unstableApi() {
        // 模拟30%失败率
        if (random.nextInt(10) < 3) {
            failureCount++;
            throw new RuntimeException("模拟接口异常 #" + failureCount);
        }
        
        return Result.success("接口正常响应");
    }

    /**
     * 测试熔断器 - 模拟高延迟接口
     */
    @GetMapping("/slow")
    @CircuitBreaker(
        key = "test_slow", 
        failureThreshold = 3, 
        timeout = 30,
        minRequestThreshold = 2
    )
    public Result<String> slowApi() throws InterruptedException {
        // 模拟随机延迟
        int delay = random.nextInt(3000) + 1000; // 1-4秒延迟
        Thread.sleep(delay);
        
        return Result.success("慢接口响应，延迟: " + delay + "ms");
    }

    /**
     * 测试熔断器 - 模拟完全失败接口
     */
    @GetMapping("/fail")
    @CircuitBreaker(
        key = "test_fail", 
        failureThreshold = 3, 
        timeout = 60,
        minRequestThreshold = 2
    )
    public Result<String> failApi() {
        throw new RuntimeException("模拟接口完全失败");
    }

    /**
     * 重置测试计数器
     */
    @PostMapping("/reset")
    public Result<String> resetTest() {
        failureCount = 0;
        return Result.success("测试计数器已重置");
    }
}
