package com.devtoolmp.mapper;

import com.devtoolmp.entity.Rating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RatingMapper {

    Rating findById(@Param("id") Long id);

    Rating findByToolIdAndUserId(@Param("toolId") Long toolId, @Param("userId") Long userId);

    List<Rating> findByToolId(@Param("toolId") Long toolId, @Param("offset") int offset, @Param("limit") int limit);

    List<Rating> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    int countByToolId(@Param("toolId") Long toolId);

    int countByUserId(@Param("userId") Long userId);

    Double getAverageScoreByToolId(@Param("toolId") Long toolId);

    Long countByToolIdAndScore(@Param("toolId") Long toolId, @Param("score") Integer score);

    List<Object[]> getScoreDistribution(@Param("toolId") Long toolId);

    void insert(Rating rating);

    void update(Rating rating);

    void deleteById(@Param("id") Long id);
}
