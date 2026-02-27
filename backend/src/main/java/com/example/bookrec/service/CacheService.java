package com.example.bookrec.service;

import com.example.bookrec.config.RedisKeys;
import com.example.bookrec.entity.BookInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private IBookInfoService bookInfoService;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 缓存穿透防护：获取图书信息（带空值缓存）
     * @param id 图书ID
     * @return 图书信息，如果不存在返回null
     */
    public BookInfo getBookWithCachePenetrationProtection(Long id) {
        String redisKey = RedisKeys.getBookKey(id);
        
        // 1. 先查Redis缓存
        String jsonString = redisTemplate.opsForValue().get(redisKey);
        
        if (jsonString != null) {
            try {
                // 缓存命中，直接返回
                if ("NULL".equals(jsonString)) {
                    return null; // 缓存的空值
                }
                return objectMapper.readValue(jsonString, BookInfo.class);
            } catch (Exception e) {
                // JSON解析失败，删除缓存重新查
                redisTemplate.delete(redisKey);
            }
        }
        
        // 2. 缓存未命中，查询数据库
        BookInfo book = bookInfoService.getById(id);
        
        // 3. 将结果写入缓存（包括空值）
        try {
            if (book != null) {
                jsonString = objectMapper.writeValueAsString(book);
                redisTemplate.opsForValue().set(redisKey, jsonString, 1, TimeUnit.HOURS);
            } else {
                // 缓存空值，防止缓存穿透，设置较短过期时间
                redisTemplate.opsForValue().set(redisKey, "NULL", 5, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            // 缓存写入失败不影响主流程
        }
        
        return book;
    }

    /**
     * 缓存击穿防护：获取图书信息（带互斥锁）
     * @param id 图书ID
     * @return 图书信息，如果不存在返回null
     */
    public BookInfo getBookWithCacheBreakdownProtection(Long id) {
        String redisKey = RedisKeys.getBookKey(id);
        String lockKey = "lock:" + redisKey;
        
        // 1. 先查Redis缓存
        String jsonString = redisTemplate.opsForValue().get(redisKey);
        if (jsonString != null) {
            try {
                if ("NULL".equals(jsonString)) {
                    return null;
                }
                return objectMapper.readValue(jsonString, BookInfo.class);
            } catch (Exception e) {
                redisTemplate.delete(redisKey);
            }
        }
        
        // 2. 尝试获取分布式锁
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 3. 获得锁，查询数据库
                BookInfo book = bookInfoService.getById(id);
                
                // 4. 写入缓存
                try {
                    if (book != null) {
                        jsonString = objectMapper.writeValueAsString(book);
                        // 雪崩防护：随机过期时间
                        long expireTime = 1 + (long)(Math.random() * 2); // 1-3小时随机
                        redisTemplate.opsForValue().set(redisKey, jsonString, expireTime, TimeUnit.HOURS);
                    } else {
                        redisTemplate.opsForValue().set(redisKey, "NULL", 5, TimeUnit.MINUTES);
                    }
                } catch (Exception e) {
                    // 缓存写入失败不影响主流程
                }
                
                return book;
            } finally {
                // 5. 释放锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 6. 未获得锁，等待并重试
            try {
                Thread.sleep(50); // 等待50ms
                // 重新查询缓存
                jsonString = redisTemplate.opsForValue().get(redisKey);
                if (jsonString != null) {
                    if ("NULL".equals(jsonString)) {
                        return null;
                    }
                    return objectMapper.readValue(jsonString, BookInfo.class);
                }
                // 如果还是没有，返回null或抛异常
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * 从缓存获取列表
     */
    public <T> List<T> getListFromCache(String key, Class<T> clazz) {
        try {
            String jsonString = redisTemplate.opsForValue().get(key);
            if (jsonString != null) {
                return objectMapper.readValue(jsonString, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
            }
        } catch (Exception e) {
            // 解析失败，删除缓存
            redisTemplate.delete(key);
        }
        return null;
    }

    /**
     * 设置列表到缓存
     */
    public <T> void setListToCache(String key, List<T> list, long minutes) {
        try {
            String jsonString = objectMapper.writeValueAsString(list);
            redisTemplate.opsForValue().set(key, jsonString, minutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            // 缓存写入失败不影响主流程
        }
    }

    /**
     * 删除图书缓存（用于数据更新时）
     */
    public void deleteBookCache(Long id) {
        String redisKey = RedisKeys.getBookKey(id);
        redisTemplate.delete(redisKey);
    }
}
