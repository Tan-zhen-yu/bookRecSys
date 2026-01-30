package com.example.bookrec.service.impl;

import com.example.bookrec.entity.BookInfo;
import com.example.bookrec.mapper.BookInfoMapper;
import com.example.bookrec.service.IBookInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 图书信息表 服务实现类
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
@Service
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements IBookInfoService {

}
