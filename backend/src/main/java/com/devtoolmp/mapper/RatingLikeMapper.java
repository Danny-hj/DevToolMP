package com.devtoolmp.mapper;

import com.devtoolmp.entity.RatingLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RatingLikeMapper {

    RatingLike findById(@Param("id") Long id);

    RatingLike findByRatingIdAndUserId(@Param("ratingId") Long ratingId, @Param("userId") Long userId);

    List<RatingLike> findByRatingId(@Param("ratingId") Long ratingId);

    void insert(RatingLike ratingLike);

    void deleteById(@Param("id") Long id);

    void deleteByRatingIdAndUserId(@Param("ratingId") Long ratingId, @Param("userId") Long userId);

    boolean existsByRatingIdAndUserId(@Param("ratingId") Long ratingId, @Param("userId") Long userId);
}
