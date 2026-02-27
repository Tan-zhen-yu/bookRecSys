package com.example.bookrec.config;

/**
 * RedisKey
 */
public class RedisKeys {
    // 1. 基础前缀
    public static final String BOOK_INFO_PREFIX = "book:info:";
    public static final String USER_LOGIN_PREFIX = "user:login:";
    public static final String ORDER_LOCK_PREFIX = "lock:order:";
    public static final String RATE_LIMIT_PREFIX = "rate:limit:";

    // 2. 提供一个静态方法来生成完整的 Key
    public static String getBookKey(Long bookId) {
        return BOOK_INFO_PREFIX + bookId;
    }
    
    /**
     * 生成限流键
     * @param clientIp 客户端IP
     * @param bookId 图书ID
     * @return 限流键
     */
    public static String getRateLimitKey(String clientIp, Long bookId) {
        return RATE_LIMIT_PREFIX + clientIp + ":" + bookId;
    }
}