package com.example.bookrec.config;


public class RedisKeys {
    // 1. 基础前缀
    public static final String BOOK_INFO_PREFIX = "book:info:";
    public static final String USER_LOGIN_PREFIX = "user:login:";
    public static final String ORDER_LOCK_PREFIX = "lock:order:";

    // 2. 提供一个静态方法来生成完整的 Key
    public static String getBookKey(Long bookId) {
        return BOOK_INFO_PREFIX + bookId;
    }
}