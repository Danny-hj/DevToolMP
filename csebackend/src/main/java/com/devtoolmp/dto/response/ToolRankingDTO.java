package com.devtoolmp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolRankingDTO {
    private Long id;
    private String name;
    private String description;
    private String codehubOwner;
    private String codehubRepo;
    private String codehubUrl;
    private Integer stars;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer installCount;
    private Double hotScore;
    private Double changePercentage;
    private List<String> tags;

    public static ToolRankingDTO fromToolDTO(ToolDTO toolDTO, Double hotScore, Double changePercentage) {
        ToolRankingDTO dto = new ToolRankingDTO();
        dto.setId(toolDTO.getId());
        dto.setName(toolDTO.getName());
        dto.setDescription(toolDTO.getDescription());
        dto.setCodehubOwner(toolDTO.getCodehubOwner());
        dto.setCodehubRepo(toolDTO.getCodehubRepo());
        dto.setCodehubUrl(toolDTO.getCodehubUrl());
        dto.setStars(toolDTO.getStars());
        dto.setViewCount(toolDTO.getViewCount());
        dto.setFavoriteCount(toolDTO.getFavoriteCount());
        dto.setInstallCount(toolDTO.getInstallCount());
        dto.setHotScore(hotScore);
        dto.setChangePercentage(changePercentage);
        dto.setTags(toolDTO.getTags());
        return dto;
    }
}
