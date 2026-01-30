package com.example.bookrec.controller;

import com.example.bookrec.common.Result;
import com.example.bookrec.entity.BookCategory;
import com.example.bookrec.service.IBookCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/category")
public class BookCategoryController {

    @Autowired
    private IBookCategoryService categoryService;

    @GetMapping("/list")
    public Result<List<BookCategory>> listAll() {
        return Result.success(categoryService.list());
    }
}