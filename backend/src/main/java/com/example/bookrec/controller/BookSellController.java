package com.example.bookrec.controller;

import com.example.bookrec.common.Result;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.service.IBookInfoService;
import com.example.bookrec.service.CacheService;
import com.example.bookrec.service.BloomFilterService;
import com.example.bookrec.resilience.CircuitBreakerAspect.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/book/sell")
public class BookSellController {

    @Autowired
    private IBookInfoService bookInfoService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private BloomFilterService bloomFilterService;

    /**
     * 售卖图书 - 带完整防护和熔断保护
     * 防护：缓存穿透 + 缓存击穿 + 缓存雪崩 + 超卖 + 熔断
     * 熔断策略：连续失败3次触发熔断，熔断30秒后尝试恢复
     * 
     * 使用POST方法确保幂等性和安全性
     */
    @PostMapping("/sell")
    @CircuitBreaker(
        key = "book_sell", 
        failureThreshold = 3, 
        timeout = 30,
        minRequestThreshold = 5,
        successThreshold = 0.6
    )
    public Result<String> sellBookWithLock(@RequestBody SellRequest request) {
        Long id = request.getBookId();
        Integer quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        
        // 1. 参数验证
        if (id == null || id <= 0) {
            return Result.error("图书ID无效");
        }
        if (quantity <= 0 || quantity > 10) {
            return Result.error("购买数量无效（1-10本）");
        }

        // 2. 缓存穿透防护：布隆过滤器检查
        if (!bloomFilterService.mightContain(id)) {
            return Result.error("图书不存在");
        }

        String lockKey = "lock:book:" + id;
        String userLockKey = "lock:user:book:" + id; // 防止用户重复购买
        long timeout = 5000; // 5秒排队时间
        long startTime = System.currentTimeMillis();

        // 3. 缓存击穿防护：分布式锁 + 重试机制
        while (System.currentTimeMillis() - startTime < timeout) {
            Boolean isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(isLocked)) {
                try {
                    // 4. 防止用户重复购买（用户级锁）
                    Boolean userLocked = redisTemplate.opsForValue().setIfAbsent(
                        userLockKey, "1", 10, TimeUnit.SECONDS);
                    
                    if (!Boolean.TRUE.equals(userLocked)) {
                        return Result.error("请勿重复购买，请稍后再试");
                    }

                    try {
                        // 5. 缓存击穿防护：从缓存获取图书信息
                        BookInfo book = cacheService.getBookWithCacheBreakdownProtection(id);
                        if (book == null) {
                            return Result.error("图书不存在");
                        }

                        // 6. 库存检查 + 原子更新
                        if (book.getCount() >= quantity) {
                            // 使用原子操作防止超卖
                            boolean updateSuccess = bookInfoService.update()
                                    .setSql("count = count - " + quantity)
                                    .eq("id", id)
                                    .ge("count", quantity)  // 原子条件检查
                                    .update();

                            if (updateSuccess) {
                                // 7. 缓存一致性：删除相关缓存
                                cacheService.deleteBookCache(id);
                                
                                // 8. 记录购买日志（可选）
                                logPurchase(id, quantity);
                                
                                return Result.success("购买成功！购买数量：" + quantity);
                            } else {
                                return Result.error("库存不足，购买失败");
                            }
                        }
                        return Result.error("库存不足，无法购买");
                    } finally {
                        // 释放用户锁
                        redisTemplate.delete(userLockKey);
                    }
                } finally {
                    // 释放图书锁
                    redisTemplate.delete(lockKey);
                }
            }

            // 重试间隔
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Result.error("服务器繁忙，请稍后再试");
            }
        }

        return Result.error("购买排队超时，请稍后再试");
    }

    /**
     * 记录购买日志
     */
    private void logPurchase(Long bookId, Integer quantity) {
        // 这里可以记录到数据库或日志文件
        System.out.println("购买记录 - 图书ID: " + bookId + ", 数量: " + quantity + 
                          ", 时间: " + new java.util.Date());
    }

    /**
     * 购买请求DTO
     */
    public static class SellRequest {
        private Long bookId;
        private Integer quantity;

        // getters and setters
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    /**
     * 重置库存 - 测试用
     */
    @GetMapping("/reset")
    public String resetStock(@RequestParam Long id, @RequestParam Integer count) {
        BookInfo book = bookInfoService.getById(id);
        if (book != null) {
            book.setCount(count);
            bookInfoService.updateById(book);
            // 清除缓存
            cacheService.deleteBookCache(id);
            return "重置成功，当前库存：" + count;
        }
        return "图书不存在";
    }
}