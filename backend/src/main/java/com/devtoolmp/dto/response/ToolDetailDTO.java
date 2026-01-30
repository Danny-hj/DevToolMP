package com.devtoolmp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private String githubOwner;
    private String githubRepo;
    private String githubUrl;
    private String version;
    private Integer stars;
    private Integer forks;
    private Integer openIssues;
    private Integer watchers;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer installCount;
    private String status;
    private List<String> tags;
    private Double averageRating;
    private Integer totalRatings;
    private boolean isFavorited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
