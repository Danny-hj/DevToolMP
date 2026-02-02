package com.devtoolmp.mapper;

import com.devtoolmp.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FavoriteMapper {

    Favorite findById(@Param("id") Long id);

    Favorite findByUserIdAndToolId(@Param("userId") String userId, @Param("toolId") Long toolId);

    List<Favorite> findByUserId(@Param("userId") String userId, @Param("offset") int offset, @Param("limit") int limit);

    int countByUserId(@Param("userId") String userId);

    void insert(Favorite favorite);

    void deleteById(@Param("id") Long id);

    void deleteByUserIdAndToolId(@Param("userId") String userId, @Param("toolId") Long toolId);

    boolean existsByUserIdAndToolId(@Param("userId") String userId, @Param("toolId") Long toolId);

    // 统计相关方法
    int countByToolId(@Param("toolId") Long toolId);

    int countByToolIdAndCreatedAtBetween(
            @Param("toolId") Long toolId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
