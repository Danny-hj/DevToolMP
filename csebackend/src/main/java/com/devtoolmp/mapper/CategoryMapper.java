package com.devtoolmp.mapper;

import com.devtoolmp.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CategoryMapper {

    Category findById(@Param("id") Long id);

    Category findByName(@Param("name") String name);

    List<Category> findAll();

    List<Category> findAllByOrderBySortOrderAsc();

    void insert(Category category);

    void update(Category category);

    void deleteById(@Param("id") Long id);

    int countAll();
}
