package com.devtoolmp.dto.response;

import com.devtoolmp.entity.Tool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolDTO {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
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
    private Integer viewCountYesterday;
    private Integer favoriteCountYesterday;
    private Integer installCountYesterday;
    private BigDecimal hotScoreDaily;
    private BigDecimal hotScoreWeekly;
    private BigDecimal hotScoreAlltime;
    private String status;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ToolDTO fromEntity(Tool tool, String categoryName, List<String> tags) {
        ToolDTO dto = new ToolDTO();
        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setDescription(tool.getDescription());
        dto.setCategoryId(tool.getCategoryId());
        dto.setCategoryName(categoryName);
        dto.setGithubOwner(tool.getGithubOwner());
        dto.setGithubRepo(tool.getGithubRepo());
        dto.setGithubUrl(tool.getGitHubUrl());
        dto.setVersion(tool.getVersion());
        dto.setStars(tool.getStars());
        dto.setForks(tool.getForks());
        dto.setOpenIssues(tool.getOpenIssues());
        dto.setWatchers(tool.getWatchers());
        dto.setViewCount(tool.getViewCount());
        dto.setFavoriteCount(tool.getFavoriteCount());
        dto.setInstallCount(tool.getInstallCount());
        dto.setViewCountYesterday(tool.getViewCountYesterday());
        dto.setFavoriteCountYesterday(tool.getFavoriteCountYesterday());
        dto.setInstallCountYesterday(tool.getInstallCountYesterday());
        dto.setHotScoreDaily(tool.getHotScoreDaily());
        dto.setHotScoreWeekly(tool.getHotScoreWeekly());
        dto.setHotScoreAlltime(tool.getHotScoreAlltime());
        dto.setStatus(tool.getStatus());
        dto.setTags(tags);
        dto.setCreatedAt(tool.getCreatedAt());
        dto.setUpdatedAt(tool.getUpdatedAt());
        return dto;
    }
}
