package com.devtoolmp.service;

import com.devtoolmp.dto.request.RatingCreateRequest;
import com.devtoolmp.dto.response.RatingDTO;
import com.devtoolmp.dto.response.CommentReplyDTO;
import com.devtoolmp.dto.response.RatingStatisticsDTO;
import com.devtoolmp.dto.response.PageResponse;
import com.devtoolmp.entity.*;
import com.devtoolmp.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingService {

    @Autowired
    private RatingMapper ratingMapper;

    @Autowired
    private CommentReplyMapper commentReplyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ToolMapper toolMapper;

    public RatingDTO getRatingById(Long ratingId) {
        Rating rating = ratingMapper.findById(ratingId);
        if (rating == null) {
            throw new RuntimeException("Rating not found");
        }
        return convertToDTO(rating);
    }

    public PageResponse<RatingDTO> getRatingsByToolId(Long toolId, int page, int size) {
        int offset = page * size;
        List<Rating> ratings = ratingMapper.findByToolId(toolId, offset, size);
        int total = ratingMapper.countByToolId(toolId);

        List<RatingDTO> ratingDTOs = ratings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(ratingDTOs, page, size, (long) total);
    }

    public RatingStatisticsDTO getRatingStatistics(Long toolId) {
        Double averageScore = ratingMapper.getAverageScoreByToolId(toolId);
        Long totalRatings = (long) ratingMapper.countByToolId(toolId);

        Long fiveStarCount = ratingMapper.countByToolIdAndScore(toolId, 5);
        Long fourStarCount = ratingMapper.countByToolIdAndScore(toolId, 4);
        Long threeStarCount = ratingMapper.countByToolIdAndScore(toolId, 3);
        Long twoStarCount = ratingMapper.countByToolIdAndScore(toolId, 2);
        Long oneStarCount = ratingMapper.countByToolIdAndScore(toolId, 1);

        RatingStatisticsDTO dto = new RatingStatisticsDTO();
        dto.setAverageScore(averageScore != null ? Math.round(averageScore * 10.0) / 10.0 : 0.0);
        dto.setTotalRatings(totalRatings != null ? totalRatings : 0L);
        dto.setFiveStarCount(fiveStarCount != null ? fiveStarCount : 0L);
        dto.setFourStarCount(fourStarCount != null ? fourStarCount : 0L);
        dto.setThreeStarCount(threeStarCount != null ? threeStarCount : 0L);
        dto.setTwoStarCount(twoStarCount != null ? twoStarCount : 0L);
        dto.setOneStarCount(oneStarCount != null ? oneStarCount : 0L);
        return dto;
    }

    @Transactional
    public RatingDTO createRating(Long toolId, Long userId, Integer score, String comment) {
        Rating existingRating = ratingMapper.findByToolIdAndUserId(toolId, userId);
        if (existingRating != null) {
            throw new RuntimeException("User has already rated this tool");
        }

        Rating rating = new Rating();
        rating.setToolId(toolId);
        rating.setUserId(userId);
        rating.setScore(score);
        rating.setComment(comment);
        rating.prePersist();
        ratingMapper.insert(rating);
        return convertToDTO(rating);
    }

    @Transactional
    public RatingDTO updateRating(Long ratingId, Long userId, Integer score, String comment) {
        Rating rating = ratingMapper.findById(ratingId);
        if (rating == null) {
            throw new RuntimeException("Rating not found");
        }
        if (!rating.getUserId().equals(userId)) {
            throw new RuntimeException("User can only update their own rating");
        }
        rating.setScore(score);
        rating.setComment(comment);
        rating.preUpdate();
        ratingMapper.update(rating);
        return convertToDTO(rating);
    }

    @Transactional
    public void deleteRating(Long ratingId, Long userId) {
        Rating rating = ratingMapper.findById(ratingId);
        if (rating == null) {
            throw new RuntimeException("Rating not found");
        }
        if (!rating.getUserId().equals(userId)) {
            throw new RuntimeException("User can only delete their own rating");
        }
        commentReplyMapper.deleteByRatingId(ratingId);
        ratingMapper.deleteById(ratingId);
    }

    public List<CommentReplyDTO> getRepliesByRatingId(Long ratingId) {
        List<CommentReply> replies = commentReplyMapper.findByRatingIdOrderByCreatedAtAsc(ratingId);
        return replies.stream().map(this::convertToReplyDTO).collect(Collectors.toList());
    }

    @Transactional
    public CommentReplyDTO createReply(Long ratingId, Long userId, String content) {
        Rating rating = ratingMapper.findById(ratingId);
        if (rating == null) {
            throw new RuntimeException("Rating not found");
        }

        CommentReply reply = new CommentReply();
        reply.setRatingId(ratingId);
        reply.setUserId(userId);
        reply.setContent(content);
        reply.prePersist();
        commentReplyMapper.insert(reply);
        return convertToReplyDTO(reply);
    }

    @Transactional
    public void deleteReply(Long replyId, Long userId) {
        CommentReply reply = commentReplyMapper.findById(replyId);
        if (reply == null) {
            throw new RuntimeException("Reply not found");
        }
        if (!reply.getUserId().equals(userId)) {
            throw new RuntimeException("User can only delete their own reply");
        }
        commentReplyMapper.deleteById(replyId);
    }

    private RatingDTO convertToDTO(Rating rating) {
        User user = userMapper.findById(rating.getUserId());
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setToolId(rating.getToolId());
        dto.setUserId(rating.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        }
        dto.setScore(rating.getScore());
        dto.setComment(rating.getComment());
        dto.setLikes(0);
        dto.setCreatedAt(rating.getCreatedAt());
        dto.setUpdatedAt(rating.getUpdatedAt());
        return dto;
    }

    private CommentReplyDTO convertToReplyDTO(CommentReply reply) {
        User user = userMapper.findById(reply.getUserId());
        User replyToUser = reply.getReplyToUserId() != null ?
                userMapper.findById(reply.getReplyToUserId()) : null;

        CommentReplyDTO dto = new CommentReplyDTO();
        dto.setId(reply.getId());
        dto.setRatingId(reply.getRatingId());
        dto.setUserId(reply.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        }
        if (replyToUser != null) {
            dto.setReplyToUsername(replyToUser.getUsername());
            dto.setReplyToUserId(replyToUser.getId());
        }
        dto.setContent(reply.getContent());
        dto.setCreatedAt(reply.getCreatedAt());
        return dto;
    }
}
