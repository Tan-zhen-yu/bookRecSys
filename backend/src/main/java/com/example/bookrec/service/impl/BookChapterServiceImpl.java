package com.example.bookrec.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookrec.entity.BookChapter;
import com.example.bookrec.mapper.BookChapterMapper;
import com.example.bookrec.service.IBookChapterService;
import org.springframework.stereotype.Service;

@Service
public class BookChapterServiceImpl extends ServiceImpl<BookChapterMapper, BookChapter> implements IBookChapterService {}