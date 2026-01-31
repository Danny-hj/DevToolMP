package com.devtoolmp.service;

import com.devtoolmp.dto.response.ToolDTO;
import com.devtoolmp.dto.response.ToolRankingDTO;
import com.devtoolmp.dto.response.PageResponse;
import com.devtoolmp.entity.Category;
import com.devtoolmp.entity.Tool;
import com.devtoolmp.entity.ToolTag;
import com.devtoolmp.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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

    @Cacheable(value = "dailyRanking", key = "'daily'")
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

    public PageResponse<ToolRankingDTO> getDailyRanking(int page, int size) {
        int offset = page * size;
        List<Tool> tools = toolMapper.findByStatusOrderByHotScoreDailyDescWithPage("active", offset, size);
        int total = toolMapper.countByStatus("active");

        List<ToolRankingDTO> rankingDTOs = tools.stream().map(tool -> {
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

        return PageResponse.of(rankingDTOs, page, size, (long) total);
    }

    @Cacheable(value = "weeklyRanking", key = "'weekly'")
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

    public PageResponse<ToolRankingDTO> getWeeklyRanking(int page, int size) {
        int offset = page * size;
        List<Tool> tools = toolMapper.findByStatusOrderByHotScoreWeeklyDescWithPage("active", offset, size);
        int total = toolMapper.countByStatus("active");

        List<ToolRankingDTO> rankingDTOs = tools.stream().map(tool -> {
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

        return PageResponse.of(rankingDTOs, page, size, (long) total);
    }

    @Cacheable(value = "allTimeRanking", key = "'alltime'")
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

    public PageResponse<ToolRankingDTO> getAllTimeRanking(int page, int size) {
        int offset = page * size;
        List<Tool> tools = toolMapper.findByStatusOrderByHotScoreAlltimeDescWithPage("active", offset, size);
        int total = toolMapper.countByStatus("active");

        List<ToolRankingDTO> rankingDTOs = tools.stream().map(tool -> {
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

        return PageResponse.of(rankingDTOs, page, size, (long) total);
    }

    /**
     * 获取趋势榜(24小时变化最快)
     */
    @Cacheable(value = "trendingRanking", key = "'trending'")
    public List<ToolRankingDTO> getTrendingRankings() {
        List<Tool> tools = toolMapper.findTop20ByStatusOrderByHotScoreDailyDesc();
        return tools.stream()
                .filter(tool -> tool.getHotScoreDaily() != null && tool.getHotScoreDaily().compareTo(BigDecimal.ZERO) > 0)
                .map(tool -> {
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
                })
                .sorted((a, b) -> Double.compare(b.getChangePercentage(), a.getChangePercentage()))
                .collect(Collectors.toList());
    }

    /**
     * 批量更新热度分数
     * 定时任务:每10分钟执行一次
     */
    @Transactional
    @Scheduled(cron = "0 */10 * * * *")
    public void updateHotScores() {
        List<Tool> tools = toolMapper.findAll();
        for (Tool tool : tools) {
            BigDecimal dailyScore = calculateHotScore(tool, "daily");
            BigDecimal weeklyScore = calculateHotScore(tool, "weekly");
            BigDecimal alltimeScore = calculateHotScore(tool, "alltime");

            tool.setHotScoreDaily(dailyScore);
            tool.setHotScoreWeekly(weeklyScore);
            tool.setHotScoreAlltime(alltimeScore);
            tool.setUpdatedAt(LocalDateTime.now());

            toolMapper.update(tool);
        }
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

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
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
