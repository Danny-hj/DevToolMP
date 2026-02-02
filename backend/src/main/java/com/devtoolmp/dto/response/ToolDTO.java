package com.devtoolmp.dto.response;

import com.devtoolmp.entity.Codehub;
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ToolDTO fromEntity(Tool tool, String categoryName, List<String> tags, Codehub codehub) {
        ToolDTO dto = new ToolDTO();
        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setDescription(tool.getDescription());
        dto.setCategoryId(tool.getCategoryId());
        dto.setCategoryName(categoryName);
        dto.setCodehubId(tool.getCodehubId());

        // 设置 codehub 信息
        if (codehub != null) {
            dto.setCodehubOwner(codehub.getOwner());
            dto.setCodehubRepo(codehub.getRepo());
            dto.setCodehubUrl(codehub.getUrl());
            dto.setVersion(codehub.getVersion());
            dto.setStars(codehub.getStars());
            dto.setForks(codehub.getForks());
            dto.setOpenIssues(codehub.getOpenIssues());
            dto.setWatchers(codehub.getWatchers());
        }

        // 统计数据需要在 service 层动态设置
        dto.setStatus(tool.getStatus());
        dto.setTags(tags);
        dto.setCreateTime(tool.getCreateTime());
        dto.setUpdateTime(tool.getUpdateTime());
        return dto;
    }
}

