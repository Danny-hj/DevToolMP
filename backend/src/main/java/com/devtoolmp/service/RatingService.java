package com.devtoolmp.service;

import com.devtoolmp.dto.request.RatingCreateRequest;
import com.devtoolmp.dto.response.RatingDTO;
import com.devtoolmp.dto.response.CommentReplyDTO;
import com.devtoolmp.dto.response.RatingStatisticsDTO;
import com.devtoolmp.entity.*;
import com.devtoolmp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CommentReplyRepository commentReplyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ToolRepository toolRepository;

    public RatingDTO getRatingById(Long ratingId) {
        Optional<Rating> ratingOptional = ratingRepository.findById(ratingId);
        if (ratingOptional.isEmpty()) {
            throw new RuntimeException("Rating not found");
        }
        return convertToDTO(ratingOptional.get());
    }

    public Page<RatingDTO> getRatingsByToolId(Long toolId, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Rating> ratings = ratingRepository.findByToolId(toolId, pageable);
        return ratings.map(this::convertToDTO);
    }

    public RatingStatisticsDTO getRatingStatistics(Long toolId) {
        Double averageScore = ratingRepository.getAverageScoreByToolId(toolId);
        Long totalRatings = ratingRepository.countByToolId(toolId);

        Long fiveStarCount = ratingRepository.countByToolIdAndScore(toolId, 5);
        Long fourStarCount = ratingRepository.countByToolIdAndScore(toolId, 4);
        Long threeStarCount = ratingRepository.countByToolIdAndScore(toolId, 3);
        Long twoStarCount = ratingRepository.countByToolIdAndScore(toolId, 2);
        Long oneStarCount = ratingRepository.countByToolIdAndScore(toolId, 1);

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
        Optional<Rating> existingRating = ratingRepository.findByToolIdAndUserId(toolId, userId);
        if (existingRating.isPresent()) {
            throw new RuntimeException("User has already rated this tool");
        }

        Rating rating = new Rating();
        rating.setToolId(toolId);
        rating.setUserId(userId);
        rating.setScore(score);
        rating.setComment(comment);
        Rating savedRating = ratingRepository.save(rating);
        return convertToDTO(savedRating);
    }

    @Transactional
    public RatingDTO updateRating(Long ratingId, Long userId, Integer score, String comment) {
        Optional<Rating> ratingOptional = ratingRepository.findById(ratingId);
        if (ratingOptional.isEmpty()) {
            throw new RuntimeException("Rating not found");
        }
        Rating rating = ratingOptional.get();
        if (!rating.getUserId().equals(userId)) {
            throw new RuntimeException("User can only update their own rating");
        }
        rating.setScore(score);
        rating.setComment(comment);
        Rating updatedRating = ratingRepository.save(rating);
        return convertToDTO(updatedRating);
    }

    @Transactional
    public void deleteRating(Long ratingId, Long userId) {
        Optional<Rating> ratingOptional = ratingRepository.findById(ratingId);
        if (ratingOptional.isEmpty()) {
            throw new RuntimeException("Rating not found");
        }
        Rating rating = ratingOptional.get();
        if (!rating.getUserId().equals(userId)) {
            throw new RuntimeException("User can only delete their own rating");
        }
        commentReplyRepository.deleteByRatingId(ratingId);
        ratingRepository.delete(rating);
    }

    public List<CommentReplyDTO> getRepliesByRatingId(Long ratingId) {
        List<CommentReply> replies = commentReplyRepository.findByRatingIdOrderByCreatedAtAsc(ratingId);
        return replies.stream().map(this::convertToReplyDTO).collect(Collectors.toList());
    }

    @Transactional
    public CommentReplyDTO createReply(Long ratingId, Long userId, String content) {
        Optional<Rating> ratingOptional = ratingRepository.findById(ratingId);
        if (ratingOptional.isEmpty()) {
            throw new RuntimeException("Rating not found");
        }

        CommentReply reply = new CommentReply();
        reply.setRatingId(ratingId);
        reply.setUserId(userId);
        reply.setContent(content);
        CommentReply savedReply = commentReplyRepository.save(reply);
        return convertToReplyDTO(savedReply);
    }

    @Transactional
    public void deleteReply(Long replyId, Long userId) {
        Optional<CommentReply> replyOptional = commentReplyRepository.findById(replyId);
        if (replyOptional.isEmpty()) {
            throw new RuntimeException("Reply not found");
        }
        CommentReply reply = replyOptional.get();
        if (!reply.getUserId().equals(userId)) {
            throw new RuntimeException("User can only delete their own reply");
        }
        commentReplyRepository.delete(reply);
    }

    private RatingDTO convertToDTO(Rating rating) {
        Optional<User> userOptional = userRepository.findById(rating.getUserId());
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setToolId(rating.getToolId());
        dto.setUserId(rating.getUserId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
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
        Optional<User> userOptional = userRepository.findById(reply.getUserId());
        Optional<User> replyToUserOptional = reply.getReplyToUserId() != null ?
                userRepository.findById(reply.getReplyToUserId()) : Optional.empty();

        CommentReplyDTO dto = new CommentReplyDTO();
        dto.setId(reply.getId());
        dto.setRatingId(reply.getRatingId());
        dto.setUserId(reply.getUserId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        }
        if (replyToUserOptional.isPresent()) {
            User replyToUser = replyToUserOptional.get();
            dto.setReplyToUsername(replyToUser.getUsername());
            dto.setReplyToUserId(replyToUser.getId());
        }
        dto.setContent(reply.getContent());
        dto.setCreatedAt(reply.getCreatedAt());
        return dto;
    }
}
