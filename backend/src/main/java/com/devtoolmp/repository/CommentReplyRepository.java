package com.devtoolmp.repository;

import com.devtoolmp.entity.CommentReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentReplyRepository extends JpaRepository<CommentReply, Long> {

    List<CommentReply> findByRatingIdOrderByCreatedAtAsc(Long ratingId);

    void deleteByRatingId(Long ratingId);
}
