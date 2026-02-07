package com.devtoolmp.schema;

import com.devtoolmp.entity.Category;
import com.devtoolmp.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理 REST Schema
 *
 * 模拟 ServiceComb 的 @RestSchema 功能
 * 使用标准 Spring Boot 注解实现
 */
@Slf4j
@RestController
@RequestMapping("/webapi/toolmarket/v1/categories")
public class CategorySchema {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        log.info("获取所有分类");
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        log.info("获取分类: id={}", id);
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }
}
