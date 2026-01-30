package com.devtoolmp.mapper;

import com.devtoolmp.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findById(Long id);

    User findByEmail(String email);

    User findByUsername(String username);

    void insert(User user);

    void update(User user);

    void deleteById(Long id);
}
