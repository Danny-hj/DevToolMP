package com.devtoolmp.service;

import com.devtoolmp.entity.Category;
import com.devtoolmp.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<Category> getAllCategories() {
        return categoryMapper.findAllByOrderBySortOrderAsc();
    }

    public Category getCategoryById(Long id) {
        return categoryMapper.findById(id);
    }

    public Category getCategoryByName(String name) {
        return categoryMapper.findByName(name);
    }

    @Transactional
    public Category createCategory(Category category) {
        category.prePersist();
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category existing = getCategoryById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在: " + id);
        }
        category.setId(id);
        categoryMapper.update(category);
        return category;
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    public int countAll() {
        return categoryMapper.countAll();
    }
}
