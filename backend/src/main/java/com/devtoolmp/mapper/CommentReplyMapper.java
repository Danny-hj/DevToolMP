package com.devtoolmp.mapper;

import com.devtoolmp.entity.CommentReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentReplyMapper {

    CommentReply findById(@Param("id") Long id);

    List<CommentReply> findByRatingIdOrderByCreatedAtAsc(@Param("ratingId") Long ratingId);

    void insert(CommentReply commentReply);

    void deleteById(@Param("id") Long id);

    void deleteByRatingId(@Param("ratingId") Long ratingId);
}
