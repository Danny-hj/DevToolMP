package com.devtoolmp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tools")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "github_owner", length = 100)
    private String githubOwner;

    @Column(name = "github_repo", length = 100)
    private String githubRepo;

    @Column(length = 50)
    private String version;

    private Integer stars = 0;

    private Integer forks = 0;

    @Column(name = "open_issues")
    private Integer openIssues = 0;

    private Integer watchers = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    @Column(name = "install_count")
    private Integer installCount = 0;

    @Column(name = "view_count_yesterday")
    private Integer viewCountYesterday = 0;

    @Column(name = "favorite_count_yesterday")
    private Integer favoriteCountYesterday = 0;

    @Column(name = "install_count_yesterday")
    private Integer installCountYesterday = 0;

    @Column(name = "hot_score_daily", precision = 10, scale = 2)
    private BigDecimal hotScoreDaily = BigDecimal.ZERO;

    @Column(name = "hot_score_weekly", precision = 10, scale = 2)
    private BigDecimal hotScoreWeekly = BigDecimal.ZERO;

    @Column(name = "hot_score_alltime", precision = 10, scale = 2)
    private BigDecimal hotScoreAlltime = BigDecimal.ZERO;

    @Column(length = 20)
    private String status = "active";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getGitHubUrl() {
        if (githubOwner != null && githubRepo != null) {
            return "https://github.com/" + githubOwner + "/" + githubRepo;
        }
        return null;
    }
}
