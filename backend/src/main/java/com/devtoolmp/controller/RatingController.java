package com.devtoolmp.controller;

import com.devtoolmp.dto.request.RatingCreateRequest;
import com.devtoolmp.dto.request.CommentReplyRequest;
import com.devtoolmp.dto.response.RatingDTO;
import com.devtoolmp.dto.response.CommentReplyDTO;
import com.devtoolmp.dto.response.RatingStatisticsDTO;
import com.devtoolmp.dto.response.PageResponse;
import com.devtoolmp.service.RatingService;
import com.devtoolmp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/{ratingId}")
    public ResponseEntity<RatingDTO> getRating(@PathVariable Long ratingId) {
        RatingDTO rating = ratingService.getRatingById(ratingId);
        return ResponseEntity.ok(rating);
    }

    @GetMapping("/tool/{toolId}")
    public ResponseEntity<PageResponse<RatingDTO>> getRatingsByToolId(
            @PathVariable Long toolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<RatingDTO> ratings = ratingService.getRatingsByToolId(toolId, page, size);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/tool/{toolId}/statistics")
    public ResponseEntity<RatingStatisticsDTO> getRatingStatistics(@PathVariable Long toolId) {
        RatingStatisticsDTO statistics = ratingService.getRatingStatistics(toolId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{ratingId}/replies")
    public ResponseEntity<List<CommentReplyDTO>> getRepliesByRatingId(@PathVariable Long ratingId) {
        List<CommentReplyDTO> replies = ratingService.getRepliesByRatingId(ratingId);
        return ResponseEntity.ok(replies);
    }

    @PostMapping("/tool/{toolId}")
    public ResponseEntity<RatingDTO> createRating(
            @PathVariable Long toolId,
            @RequestBody RatingCreateRequest request,
            HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        RatingDTO rating = ratingService.createRating(toolId, userId, request.getScore(), request.getComment());
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    @PutMapping("/{ratingId}")
    public ResponseEntity<RatingDTO> updateRating(
            @PathVariable Long ratingId,
            @RequestBody RatingCreateRequest request,
            HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        RatingDTO rating = ratingService.updateRating(ratingId, userId, request.getScore(), request.getComment());
        return ResponseEntity.ok(rating);
    }

    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable Long ratingId,
            HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        ratingService.deleteRating(ratingId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{ratingId}/replies")
    public ResponseEntity<CommentReplyDTO> createReply(
            @PathVariable Long ratingId,
            @RequestBody CommentReplyRequest request,
            HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        CommentReplyDTO reply = ratingService.createReply(ratingId, userId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @PathVariable Long replyId,
            HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        ratingService.deleteReply(replyId, userId);
        return ResponseEntity.noContent().build();
    }
}
