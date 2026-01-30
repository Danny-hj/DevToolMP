package com.devtoolmp.service;

import com.devtoolmp.dto.response.ToolDTO;
import com.devtoolmp.dto.response.ToolRankingDTO;
import com.devtoolmp.entity.Category;
import com.devtoolmp.entity.Tool;
import com.devtoolmp.entity.ToolTag;
import com.devtoolmp.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingService {

    @Autowired
    private ToolMapper toolMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ToolTagMapper toolTagMapper;

    public List<ToolRankingDTO> getDailyRanking() {
        List<Tool> tools = toolMapper.findTop20ByStatusOrderByHotScoreDailyDesc();
        return tools.stream().map(tool -> {
            Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
            String categoryName = category != null ? category.getName() : null;
            List<ToolTag> toolTags = toolTagMapper.findByToolId(tool.getId());
            List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
            ToolDTO toolDTO = ToolDTO.fromEntity(tool, categoryName, tags);
            Double changePercentage = calculateChangePercentage(
                    tool.getViewCount(), tool.getViewCountYesterday(),
                    tool.getFavoriteCount(), tool.getFavoriteCountYesterday(),
                    tool.getInstallCount(), tool.getInstallCountYesterday()
            );
            return ToolRankingDTO.fromToolDTO(toolDTO, tool.getHotScoreDaily(), changePercentage);
        }).collect(Collectors.toList());
    }

    public List<ToolRankingDTO> getWeeklyRanking() {
        List<Tool> tools = toolMapper.findTop20ByStatusOrderByHotScoreWeeklyDesc();
        return tools.stream().map(tool -> {
            Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
            String categoryName = category != null ? category.getName() : null;
            List<ToolTag> toolTags = toolTagMapper.findByToolId(tool.getId());
            List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
            ToolDTO toolDTO = ToolDTO.fromEntity(tool, categoryName, tags);
            Double changePercentage = calculateChangePercentage(
                    tool.getViewCount(), tool.getViewCountYesterday(),
                    tool.getFavoriteCount(), tool.getFavoriteCountYesterday(),
                    tool.getInstallCount(), tool.getInstallCountYesterday()
            );
            return ToolRankingDTO.fromToolDTO(toolDTO, tool.getHotScoreWeekly(), changePercentage);
        }).collect(Collectors.toList());
    }

    public List<ToolRankingDTO> getAllTimeRanking() {
        List<Tool> tools = toolMapper.findTop20ByStatusOrderByHotScoreAlltimeDesc();
        return tools.stream().map(tool -> {
            Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
            String categoryName = category != null ? category.getName() : null;
            List<ToolTag> toolTags = toolTagMapper.findByToolId(tool.getId());
            List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
            ToolDTO toolDTO = ToolDTO.fromEntity(tool, categoryName, tags);
            Double changePercentage = calculateChangePercentage(
                    tool.getViewCount(), tool.getViewCountYesterday(),
                    tool.getFavoriteCount(), tool.getFavoriteCountYesterday(),
                    tool.getInstallCount(), tool.getInstallCountYesterday()
            );
            return ToolRankingDTO.fromToolDTO(toolDTO, tool.getHotScoreAlltime(), changePercentage);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void calculateHotScores() {
        // Note: This method needs to be implemented differently since MyBatis doesn't have findAll()
        // For now, this is a placeholder that would need to be implemented based on actual needs
        // You might need to add a findAll() method to ToolMapper
    }

    private BigDecimal calculateHotScore(Tool tool, String period) {
        double viewWeight, favoriteWeight, installWeight;

        switch (period) {
            case "daily":
                viewWeight = 0.2;
                favoriteWeight = 0.3;
                installWeight = 0.5;
                break;
            case "weekly":
                viewWeight = 0.3;
                favoriteWeight = 0.3;
                installWeight = 0.4;
                break;
            case "alltime":
                viewWeight = 0.4;
                favoriteWeight = 0.3;
                installWeight = 0.3;
                break;
            default:
                viewWeight = 0.33;
                favoriteWeight = 0.33;
                installWeight = 0.34;
        }

        double score = (tool.getViewCount() * viewWeight) +
                (tool.getFavoriteCount() * favoriteWeight * 10) +
                (tool.getInstallCount() * installWeight * 5);

        return BigDecimal.valueOf(score);
    }

    private Double calculateChangePercentage(Integer currentViews, Integer yesterdayViews,
                                              Integer currentFavorites, Integer yesterdayFavorites,
                                              Integer currentInstalls, Integer yesterdayInstalls) {
        double currentValue = currentViews + (currentFavorites * 10) + (currentInstalls * 5);
        double yesterdayValue = yesterdayViews + (yesterdayFavorites * 10) + (yesterdayInstalls * 5);

        if (yesterdayValue == 0) {
            return currentValue > 0 ? 100.0 : 0.0;
        }

        return ((currentValue - yesterdayValue) / yesterdayValue) * 100;
    }
}
