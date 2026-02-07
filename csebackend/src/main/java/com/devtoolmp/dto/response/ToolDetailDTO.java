package com.devtoolmp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private Long codehubId;
    private String codehubOwner;
    private String codehubRepo;
    private String codehubUrl;
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
    private Double averageRating;
    private Integer totalRatings;

    @JsonProperty("isFavorited")
    private boolean isFavorited;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
