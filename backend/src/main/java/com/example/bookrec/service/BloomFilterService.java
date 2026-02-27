package com.example.bookrec.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.List;

@Service
public class BloomFilterService {

    @Autowired
    private IBookInfoService bookInfoService;

    // 本地布隆过滤器（适用于数据量不大的场景）
    private BloomFilter<String> localBloomFilter;

    @PostConstruct
    public void init() {
        // 初始化本地布隆过滤器
        // 预计插入100万条数据，误判率0.1%
        localBloomFilter = BloomFilter.create(
            Funnels.stringFunnel(Charset.defaultCharset()),
            1000000,
            0.001
        );
        
        // 加载所有存在的图书ID到布隆过滤器
        loadExistingBookIds();
    }

    /**
     * 加载所有存在的图书ID到布隆过滤器
     */
    private void loadExistingBookIds() {
        try {
            // 查询所有图书ID（这里简化处理，实际应该分批查询）
            List<Long> bookIds = bookInfoService.listIds();
            
            for (Long bookId : bookIds) {
                String idStr = String.valueOf(bookId);
                localBloomFilter.put(idStr);
                // 本地布隆过滤器已经足够，Redis布隆过滤器需要RedisBloomFilter插件
                // 这里简化处理，只使用本地布隆过滤器
            }
        } catch (Exception e) {
            // 初始化失败不影响主流程
            e.printStackTrace();
        }
    }

    /**
     * 检查图书ID是否可能存在
     * @param bookId 图书ID
     * @return true表示可能存在，false表示一定不存在
     */
    public boolean mightContain(Long bookId) {
        String idStr = String.valueOf(bookId);
        return localBloomFilter.mightContain(idStr);
    }

    /**
     * 添加图书ID到布隆过滤器
     */
    public void add(Long bookId) {
        String idStr = String.valueOf(bookId);
        localBloomFilter.put(idStr);
    }

    /**
     * 从布隆过滤器中删除（布隆过滤器不支持删除，需要重建）
     * 这里只是标记，实际需要重建布隆过滤器
     */
    public void delete(Long bookId) {
        // 布隆过滤器不支持删除操作
        // 可以考虑使用Counting Bloom Filter或者定期重建
        // 这里简化处理，不做任何操作
    }
}
