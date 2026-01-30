package com.example.bookrec.service.impl;

import com.example.bookrec.entity.BookCategory;
import com.example.bookrec.mapper.BookCategoryMapper;
import com.example.bookrec.service.IBookCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 图书分类表 服务实现类
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
@Service
public class BookCategoryServiceImpl extends ServiceImpl<BookCategoryMapper, BookCategory> implements IBookCategoryService {

}
