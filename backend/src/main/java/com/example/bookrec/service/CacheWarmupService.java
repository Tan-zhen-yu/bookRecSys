package com.example.bookrec.service;

import com.example.bookrec.entity.BookInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 缓存预热服务
 * 在应用启动时预热热点数据
 */
@Service
public class CacheWarmupService implements ApplicationRunner {

    @Autowired
    private IBookInfoService bookInfoService;

    @Autowired
    private CacheService cacheService;

    /**
     * 预热排行榜数据
     */
    @PostConstruct
    public void warmupRankData() {
        try {
            // 异步预热，避免阻塞应用启动
            new Thread(this::doWarmup).start();
        } catch (Exception e) {
            // 预热失败不影响应用启动
        }
    }

    private void doWarmup() {
        try {
            // 等待应用完全启动
            Thread.sleep(5000);
            
            // 预热高分榜
            warmupRatingRank();
            
            // 预热热度榜  
            warmupHotRank();
            
            // 预热新书榜
            warmupNewRank();
            
        } catch (Exception e) {
            // 预热失败记录日志
        }
    }

    private void warmupRatingRank() {
        // 查询高分榜数据
        List<BookInfo> ratingRank = bookInfoService.lambdaQuery()
                .ge(BookInfo::getRatingCount, 5)
                .orderByDesc(BookInfo::getRatingAvg)
                .last("limit 10")
                .list();
        
        // 缓存30分钟
        cacheService.setListToCache("book:rank:rating", ratingRank, 30);
    }

    private void warmupHotRank() {
        // 查询热度榜数据
        List<BookInfo> hotRank = bookInfoService.lambdaQuery()
                .orderByDesc(BookInfo::getRatingCount)
                .last("limit 10")
                .list();
        
        // 缓存30分钟
        cacheService.setListToCache("book:rank:hot", hotRank, 30);
    }

    private void warmupNewRank() {
        // 查询新书榜数据
        List<BookInfo> newRank = bookInfoService.lambdaQuery()
                .orderByDesc(BookInfo::getCreateTime)
                .last("limit 10")
                .list();
        
        // 缓存30分钟
        cacheService.setListToCache("book:rank:new", newRank, 30);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // ApplicationRunner接口实现，确保在应用启动后执行
        warmupRankData();
    }
}
