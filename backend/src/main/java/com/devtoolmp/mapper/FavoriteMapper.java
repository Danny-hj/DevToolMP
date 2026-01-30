package com.devtoolmp.mapper;

import com.devtoolmp.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    Favorite findById(@Param("id") Long id);

    Favorite findByUserIdAndToolId(@Param("userId") Long userId, @Param("toolId") Long toolId);

    List<Favorite> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    int countByUserId(@Param("userId") Long userId);

    void insert(Favorite favorite);

    void deleteById(@Param("id") Long id);

    void deleteByUserIdAndToolId(@Param("userId") Long userId, @Param("toolId") Long toolId);

    boolean existsByUserIdAndToolId(@Param("userId") Long userId, @Param("toolId") Long toolId);
}
