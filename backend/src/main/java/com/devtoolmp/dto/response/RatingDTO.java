package com.devtoolmp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDTO {
    private Long id;
    private Long toolId;
    private Long userId;
    private String username;
    private String userAvatar;
    private Integer score;
    private String comment;
    private Integer likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
