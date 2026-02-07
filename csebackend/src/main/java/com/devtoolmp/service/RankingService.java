package com.devtoolmp.service;

import com.devtoolmp.dto.response.ToolDTO;
import com.devtoolmp.dto.response.ToolRankingDTO;
import com.devtoolmp.dto.response.PageResponse;
import com.devtoolmp.entity.Category;
import com.devtoolmp.entity.Codehub;
import com.devtoolmp.entity.Tool;
import com.devtoolmp.entity.ToolTag;
import com.devtoolmp.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private CodehubMapper codehubMapper;

    @Autowired
    private ToolTagMapper toolTagMapper;

    @Autowired
    private ViewRecordMapper viewRecordMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private InstallRecordMapper installRecordMapper;

    public List<ToolRankingDTO> getDailyRanking() {
        List<Tool> allTools = toolMapper.findByStatus("active");
        return getRankingDTOs(allTools, "daily").stream()
                .limit(20)
                .collect(Collectors.toList());
    }

    public PageResponse<ToolRankingDTO> getDailyRanking(int page, int size) {
        List<Tool> allTools = toolMapper.findByStatus("active");
        List<ToolRankingDTO> rankingDTOs = getRankingDTOs(allTools, "daily");

        int total = rankingDTOs.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        List<ToolRankingDTO> pagedResult = start < total ? rankingDTOs.subList(start, end) : List.of();

        return PageResponse.of(pagedResult, page, size, (long) total);
    }

    public List<ToolRankingDTO> getWeeklyRanking() {
        List<Tool> allTools = toolMapper.findByStatus("active");
        return getRankingDTOs(allTools, "weekly").stream()
                .limit(20)
                .collect(Collectors.toList());
    }

    public PageResponse<ToolRankingDTO> getWeeklyRanking(int page, int size) {
        List<Tool> allTools = toolMapper.findByStatus("active");
        List<ToolRankingDTO> rankingDTOs = getRankingDTOs(allTools, "weekly");

        int total = rankingDTOs.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        List<ToolRankingDTO> pagedResult = start < total ? rankingDTOs.subList(start, end) : List.of();

        return PageResponse.of(pagedResult, page, size, (long) total);
    }

    public List<ToolRankingDTO> getAllTimeRanking() {
        List<Tool> allTools = toolMapper.findByStatus("active");
        return getRankingDTOs(allTools, "alltime").stream()
                .limit(20)
                .collect(Collectors.toList());
    }

    public PageResponse<ToolRankingDTO> getAllTimeRanking(int page, int size) {
        List<Tool> allTools = toolMapper.findByStatus("active");
        List<ToolRankingDTO> rankingDTOs = getRankingDTOs(allTools, "alltime");

        int total = rankingDTOs.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        List<ToolRankingDTO> pagedResult = start < total ? rankingDTOs.subList(start, end) : List.of();

        return PageResponse.of(pagedResult, page, size, (long) total);
    }

    /**
     * 获取趋势榜(24小时变化最快)
     */
    public List<ToolRankingDTO> getTrendingRankings() {
        List<Tool> allTools = toolMapper.findByStatus("active");
        return getRankingDTOs(allTools, "daily").stream()
                .filter(dto -> dto.getHotScore() > 0)
                .sorted((a, b) -> Double.compare(b.getChangePercentage(), a.getChangePercentage()))
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * 根据指定的时间范围生成排行榜
     */
    private List<ToolRankingDTO> getRankingDTOs(List<Tool> tools, String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart;
        LocalDateTime yesterdayStart = now.minusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yesterdayEnd = now.minusDays(1).withHour(23).withMinute(59).withSecond(59);

        switch (period) {
            case "daily":
                periodStart = yesterdayStart;
                break;
            case "weekly":
                periodStart = now.minusDays(7);
                break;
            case "alltime":
            default:
                // 使用10年前的时间代替MIN，避免SQL查询问题
                periodStart = now.minusYears(10);
                break;
        }

        return tools.stream().map(tool -> {
            Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
            String categoryName = category != null ? category.getName() : null;
            Codehub codehub = tool.getCodehubId() != null ? codehubMapper.findById(tool.getCodehubId()) : null;
            List<ToolTag> toolTags = toolTagMapper.findByToolId(tool.getId());
            List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());

            // 获取当前周期的统计
            int views = viewRecordMapper.countByToolIdAndCreatedAtBetween(tool.getId(), periodStart, now);
            int favorites = favoriteMapper.countByToolIdAndCreatedAtBetween(tool.getId(), periodStart, now);
            int installs = installRecordMapper.countByToolIdAndCreatedAtBetween(tool.getId(), periodStart, now);

            // 获取昨天的统计
            int viewsYesterday = viewRecordMapper.countByToolIdAndCreatedAtBetween(tool.getId(), yesterdayStart, yesterdayEnd);
            int favoritesYesterday = favoriteMapper.countByToolIdAndCreatedAtBetween(tool.getId(), yesterdayStart, yesterdayEnd);
            int installsYesterday = installRecordMapper.countByToolIdAndCreatedAtBetween(tool.getId(), yesterdayStart, yesterdayEnd);

            // 计算热度分数
            double hotScore = calculateHotScore(views, favorites, installs);

            // 计算变化百分比
            Double changePercentage = calculateChangePercentage(
                    views, viewsYesterday,
                    favorites, favoritesYesterday,
                    installs, installsYesterday
            );

            // 直接创建 ToolRankingDTO
            ToolRankingDTO dto = new ToolRankingDTO();
            dto.setId(tool.getId());
            dto.setName(tool.getName());
            dto.setDescription(tool.getDescription());
            if (codehub != null) {
                dto.setCodehubOwner(codehub.getOwner());
                dto.setCodehubRepo(codehub.getRepo());
                dto.setCodehubUrl(codehub.getUrl());
                dto.setStars(codehub.getStars());
            }
            dto.setViewCount(views);
            dto.setFavoriteCount(favorites);
            dto.setInstallCount(installs);
            dto.setHotScore(hotScore);
            dto.setChangePercentage(changePercentage);
            dto.setTags(tags);
            return dto;
        })
        .sorted((a, b) -> Double.compare(b.getHotScore(), a.getHotScore()))
        .collect(Collectors.toList());
    }

    private double calculateHotScore(int views, int favorites, int installs) {
        return views * 1.0 + favorites * 5.0 + installs * 10.0;
    }

    private Double calculateChangePercentage(int currentViews, int yesterdayViews,
                                              int currentFavorites, int yesterdayFavorites,
                                              int currentInstalls, int yesterdayInstalls) {
        double currentValue = currentViews + (currentFavorites * 10) + (currentInstalls * 5);
        double yesterdayValue = yesterdayViews + (yesterdayFavorites * 10) + (yesterdayInstalls * 5);

        if (yesterdayValue == 0) {
            return currentValue > 0 ? 100.0 : 0.0;
        }

        return ((currentValue - yesterdayValue) / yesterdayValue) * 100;
    }
}
