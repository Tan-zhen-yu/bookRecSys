package com.example.bookrec.service;

import com.example.bookrec.entity.BookInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 图书信息表 服务类
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
public interface IBookInfoService extends IService<BookInfo> {

    /**
     * 获取所有图书ID列表（用于布隆过滤器初始化）
     * @return 图书ID列表
     */
    List<Long> listIds();

}
