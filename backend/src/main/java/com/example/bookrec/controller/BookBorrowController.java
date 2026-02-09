package com.example.bookrec.controller;

import com.example.bookrec.config.RedisKeys;
import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.service.IBookInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/book/test")
public class BookBorrowController {

    @Autowired
    private IBookInfoService bookInfoService;

    @Autowired
    private StringRedisTemplate redisTemplate; // 专门处理 String 的模板

    // 需要用到 JSON 转换工具 (Jackson / Gson / Fastjson)
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 1. 【线程不安全】的借书接口 —— 用于模拟超卖
     * 压测此接口，你会发现库存变负数
     */
    @GetMapping("/borrow/unsafe")
    public String borrowUnsafe(@RequestParam Long id) {

        // 1. 查出当前书籍信息（这一步在并发下是不准的）
        String redisKey = RedisKeys.getBookKey(id);
        redisTemplate.opsForValue().get(redisKey);
        HashMap map = new HashMap();

        BookInfo book = bookInfoService.getById(id);

        // 2. 逻辑判断：只要查出来的时候库存 > 0，就让它进来
        if (book != null && book.getCount() > 0) {

            // 模拟业务处理耗时，让更多线程挤进这个 IF 块
            try { Thread.sleep(50); } catch (InterruptedException e) {}

            // 3. 【核心修改】：直接让数据库执行 count = count - 1
            // 注意：这里没有加任何 WHERE count > 0 的限制，所以会减成负数
            bookInfoService.update()
                    .setSql("count = count - 1")
                    .eq("id", id)
                    .gt("count", 0)
                    .update();

            return "借阅成功！";
        }
        return "库存不足";
    }

    /**
     * 2. 【Synchronized锁】借书接口 —— 解决超卖，但性能低
     * 压测此接口，库存严格为0，但吞吐量很低
     */
    @GetMapping("/borrow/sync")
    public synchronized String borrowSync(@RequestParam Long id) {
        BookInfo book = bookInfoService.getById(id);
        if (book != null && book.getCount() > 0) {
            book.setCount(book.getCount() - 1);
            bookInfoService.updateById(book);
            return "【同步】借阅成功，剩余库存：" + book.getCount();
        }
        return "库存不足";
    }

    /**
     * 4. 重置库存（方便反复压测）
     */
    @GetMapping("/reset")
    public String reset(@RequestParam Long id, @RequestParam Integer count) {


        BookInfo book = bookInfoService.getById(id);
        book.setCount(count);
        bookInfoService.updateById(book);
        String redisKey = RedisKeys.getBookKey(id);
        redisTemplate.delete(redisKey);
        return "重置成功，当前库存：" + count;
    }

    protected BookInfo getBookinfoFromCache(String redisKey) {
        String jsonString  = redisTemplate.opsForValue().get(redisKey);

        if(jsonString != null) {
            try {
                BookInfo bookInfo = objectMapper.readValue(jsonString,BookInfo.class);
                return bookInfo;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected void setBookinfoToCache(BookInfo bookInfo) {
        String key = RedisKeys.getBookKey(bookInfo.getId());
        String jsonString ="";
        try {
            jsonString=objectMapper.writeValueAsString(bookInfo);
        }catch (Exception e) {

        }
        redisTemplate.opsForValue().set(key, jsonString,1, TimeUnit.HOURS);
    }
}