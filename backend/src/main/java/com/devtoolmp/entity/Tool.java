package com.devtoolmp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tool {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String githubOwner;
    private String githubRepo;
    private String version;
    private Integer stars = 0;
    private Integer forks = 0;
    private Integer openIssues = 0;
    private Integer watchers = 0;
    private Integer viewCount = 0;
    private Integer favoriteCount = 0;
    private Integer installCount = 0;
    private Integer viewCountYesterday = 0;
    private Integer favoriteCountYesterday = 0;
    private Integer installCountYesterday = 0;
    private BigDecimal hotScoreDaily = BigDecimal.ZERO;
    private BigDecimal hotScoreWeekly = BigDecimal.ZERO;
    private BigDecimal hotScoreAlltime = BigDecimal.ZERO;
    private String status = "active";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getGitHubUrl() {
        if (githubOwner != null && !githubOwner.trim().isEmpty()
                && githubRepo != null && !githubRepo.trim().isEmpty()) {
            return "https://github.com/" + githubOwner + "/" + githubRepo;
        }
        return null;
    }
}
