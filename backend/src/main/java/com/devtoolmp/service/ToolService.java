package com.devtoolmp.service;

import com.devtoolmp.dto.request.ToolCreateRequest;
import com.devtoolmp.dto.request.ToolUpdateRequest;
import com.devtoolmp.dto.response.*;
import com.devtoolmp.entity.*;
import com.devtoolmp.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToolService {

    @Autowired
    private ToolMapper toolMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CodehubMapper codehubMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ViewRecordMapper viewRecordMapper;

    @Autowired
    private InstallRecordMapper installRecordMapper;

    @Autowired
    private ToolTagMapper toolTagMapper;

    @Autowired
    private RatingMapper ratingMapper;

    @Transactional
    public Tool createTool(ToolCreateRequest request) {
        // 首先创建或获取 Codehub 记录
        Codehub codehub = null;
        if (request.getGithubOwner() != null && request.getGithubRepo() != null) {
            codehub = codehubMapper.findByOwnerAndRepo(request.getGithubOwner(), request.getGithubRepo());
            if (codehub == null) {
                codehub = new Codehub();
                codehub.setOwner(request.getGithubOwner());
                codehub.setRepo(request.getGithubRepo());
                codehub.setVersion(request.getVersion());
                codehub.prePersist();
                codehubMapper.insert(codehub);
            }
        }

        Tool tool = new Tool();
        tool.setName(request.getName());
        tool.setDescription(request.getDescription());
        tool.setCategoryId(request.getCategoryId());
        tool.setCodehubId(codehub != null ? codehub.getId() : null);
        tool.setStatus(request.getStatus() != null ? request.getStatus() : "active");
        tool.prePersist();
        toolMapper.insert(tool);

        // 保存标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tagName : request.getTags()) {
                if (tagName != null && !tagName.trim().isEmpty()) {
                    ToolTag toolTag = new ToolTag();
                    toolTag.setToolId(tool.getId());
                    toolTag.setTagName(tagName.trim());
                    toolTagMapper.insert(toolTag);
                }
            }
        }

        return tool;
    }

    @Transactional
    public ToolDTO updateTool(Long id, ToolUpdateRequest request) {
        Tool tool = toolMapper.findById(id);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }
        if (request.getName() != null) {
            tool.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tool.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            tool.setCategoryId(request.getCategoryId());
        }

        // 更新 codehub 信息
        if (request.getGithubOwner() != null && request.getGithubRepo() != null) {
            Codehub codehub = codehubMapper.findByOwnerAndRepo(request.getGithubOwner(), request.getGithubRepo());
            if (codehub == null) {
                codehub = new Codehub();
                codehub.setOwner(request.getGithubOwner());
                codehub.setRepo(request.getGithubRepo());
                codehub.setVersion(request.getVersion());
                codehub.prePersist();
                codehubMapper.insert(codehub);
            } else {
                if (request.getVersion() != null) {
                    codehub.setVersion(request.getVersion());
                    codehub.preUpdate();
                    codehubMapper.update(codehub);
                }
            }
            tool.setCodehubId(codehub.getId());
        }

        tool.preUpdate();
        toolMapper.update(tool);

        // 获取分类信息和 codehub 信息
        Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
        String categoryName = category != null ? category.getName() : null;
        Codehub codehub = tool.getCodehubId() != null ? codehubMapper.findById(tool.getCodehubId()) : null;

        // 获取标签信息
        List<ToolTag> toolTags = toolTagMapper.findByToolId(tool.getId());
        List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());

        return ToolDTO.fromEntity(tool, categoryName, tags, codehub);
    }

    @Transactional
    public void deleteTool(Long id) {
        toolMapper.deleteById(id);
    }

    public ToolDTO getToolById(Long id) {
        Tool tool = toolMapper.findById(id);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }
        Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
        String categoryName = category != null ? category.getName() : null;
        Codehub codehub = tool.getCodehubId() != null ? codehubMapper.findById(tool.getCodehubId()) : null;
        List<ToolTag> toolTags = toolTagMapper.findByToolId(id);
        List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
        return ToolDTO.fromEntity(tool, categoryName, tags, codehub);
    }

    public ToolDetailDTO getToolDetailById(Long id, String userId) {
        Tool tool = toolMapper.findById(id);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }
        Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
        String categoryName = category != null ? category.getName() : null;
        Codehub codehub = tool.getCodehubId() != null ? codehubMapper.findById(tool.getCodehubId()) : null;
        List<ToolTag> toolTags = toolTagMapper.findByToolId(id);
        List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());

        Double averageRating = ratingMapper.getAverageScoreByToolId(id);
        Long totalRatings = (long) ratingMapper.countByToolId(id);

        boolean isFavorited = false;
        if (userId != null) {
            isFavorited = favoriteMapper.existsByUserIdAndToolId(userId, id);
        }

        // 动态计算统计数据
        int viewCount = viewRecordMapper.countByToolId(id);
        int favoriteCount = favoriteMapper.countByToolId(id);
        int installCount = installRecordMapper.countByToolId(id);

        // 计算昨天的数据
        LocalDateTime yesterdayStart = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yesterdayEnd = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59);
        int viewCountYesterday = viewRecordMapper.countByToolIdAndCreatedAtBetween(id, yesterdayStart, yesterdayEnd);
        int favoriteCountYesterday = favoriteMapper.countByToolIdAndCreatedAtBetween(id, yesterdayStart, yesterdayEnd);
        int installCountYesterday = installRecordMapper.countByToolIdAndCreatedAtBetween(id, yesterdayStart, yesterdayEnd);

        // 计算热度分数
        double hotScoreDaily = calculateHotScore(viewCountYesterday, favoriteCountYesterday, installCountYesterday);
        double hotScoreWeekly = calculateHotScore(
            viewRecordMapper.countByToolIdAndCreatedAtBetween(id, LocalDateTime.now().minusDays(7), LocalDateTime.now()),
            favoriteMapper.countByToolIdAndCreatedAtBetween(id, LocalDateTime.now().minusDays(7), LocalDateTime.now()),
            installRecordMapper.countByToolIdAndCreatedAtBetween(id, LocalDateTime.now().minusDays(7), LocalDateTime.now())
        );
        double hotScoreAlltime = calculateHotScore(viewCount, favoriteCount, installCount);

        ToolDetailDTO dto = new ToolDetailDTO();
        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setDescription(tool.getDescription());
        dto.setCategoryName(categoryName);

        // 设置 codehub 信息
        if (codehub != null) {
            dto.setCodehubId(codehub.getId());
            dto.setCodehubOwner(codehub.getOwner());
            dto.setCodehubRepo(codehub.getRepo());
            dto.setCodehubUrl(codehub.getUrl());
            dto.setVersion(codehub.getVersion());
            dto.setStars(codehub.getStars());
            dto.setForks(codehub.getForks());
            dto.setOpenIssues(codehub.getOpenIssues());
            dto.setWatchers(codehub.getWatchers());
        }

        dto.setViewCount(viewCount);
        dto.setFavoriteCount(favoriteCount);
        dto.setInstallCount(installCount);
        dto.setViewCountYesterday(viewCountYesterday);
        dto.setFavoriteCountYesterday(favoriteCountYesterday);
        dto.setInstallCountYesterday(installCountYesterday);
        dto.setHotScoreDaily(java.math.BigDecimal.valueOf(hotScoreDaily));
        dto.setHotScoreWeekly(java.math.BigDecimal.valueOf(hotScoreWeekly));
        dto.setHotScoreAlltime(java.math.BigDecimal.valueOf(hotScoreAlltime));
        dto.setStatus(tool.getStatus());
        dto.setTags(tags);
        dto.setAverageRating(averageRating);
        dto.setTotalRatings(totalRatings != null ? totalRatings.intValue() : 0);
        dto.setFavorited(isFavorited);
        dto.setUpdateTime(tool.getUpdateTime());
        dto.setCreateTime(tool.getCreateTime());
        return dto;
    }

    private double calculateHotScore(int views, int favorites, int installs) {
        return views * 1.0 + favorites * 5.0 + installs * 10.0;
    }

    public PageResponse<ToolDTO> getTools(int page, int size) {
        int offset = page * size;
        List<Tool> tools = toolMapper.findByStatusWithPage("active", offset, size);
        int total = toolMapper.countByStatus("active");

        List<ToolDTO> toolDTOs = tools.stream().map(tool -> {
            Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
            String categoryName = category != null ? category.getName() : null;
            Codehub codehub = tool.getCodehubId() != null ? codehubMapper.findById(tool.getCodehubId()) : null;
            List<ToolTag> toolTags = toolTagMapper.findByToolId(tool.getId());
            List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());

            // 计算热度分数（使用昨天数据）
            int viewCountYesterday = viewRecordMapper.countByToolIdAndCreatedAtBetween(
                tool.getId(),
                LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59)
            );
            int favoriteCountYesterday = favoriteMapper.countByToolIdAndCreatedAtBetween(
                tool.getId(),
                LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59)
            );
            int installCountYesterday = installRecordMapper.countByToolIdAndCreatedAtBetween(
                tool.getId(),
                LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59)
            );
            double hotScoreDaily = calculateHotScore(viewCountYesterday, favoriteCountYesterday, installCountYesterday);

            ToolDTO dto = ToolDTO.fromEntity(tool, categoryName, tags, codehub);
            dto.setHotScoreDaily(java.math.BigDecimal.valueOf(hotScoreDaily));
            return dto;
        }).collect(Collectors.toList());

        return PageResponse.of(toolDTOs, page, size, (long) total);
    }

    public PageResponse<ToolDTO> searchTools(String keyword, int page, int size) {
        List<Tool> allTools = toolMapper.searchByKeyword(keyword);
        int total = allTools.size();

        int start = page * size;
        int end = Math.min(start + size, total);
        List<Tool> tools = start < total ? allTools.subList(start, end) : List.of();

        List<ToolDTO> toolDTOs = tools.stream().map(tool -> {
            Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
            String categoryName = category != null ? category.getName() : null;
            Codehub codehub = tool.getCodehubId() != null ? codehubMapper.findById(tool.getCodehubId()) : null;
            List<ToolTag> toolTags = toolTagMapper.findByToolId(tool.getId());
            List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
            return ToolDTO.fromEntity(tool, categoryName, tags, codehub);
        }).collect(Collectors.toList());

        return PageResponse.of(toolDTOs, page, size, (long) total);
    }

    @Transactional
    public void recordView(Long toolId, String userId) {
        Tool tool = toolMapper.findById(toolId);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }

        ViewRecord viewRecord = new ViewRecord();
        viewRecord.setToolId(toolId);
        viewRecord.setUserId(userId);
        viewRecord.prePersist();
        viewRecordMapper.insert(viewRecord);
    }

    @Transactional
    public boolean toggleFavorite(Long toolId, String userId) {
        Favorite favorite = favoriteMapper.findByUserIdAndToolId(userId, toolId);
        Tool tool = toolMapper.findById(toolId);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }

        if (favorite != null) {
            favoriteMapper.deleteByUserIdAndToolId(userId, toolId);
            return false;
        } else {
            favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setToolId(toolId);
            favorite.prePersist();
            favoriteMapper.insert(favorite);
            return true;
        }
    }

    public boolean isFavorited(Long toolId, String userId) {
        return favoriteMapper.existsByUserIdAndToolId(userId, toolId);
    }

    @Transactional
    public void recordInstall(Long toolId, String userId) {
        Tool tool = toolMapper.findById(toolId);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }

        InstallRecord installRecord = new InstallRecord();
        installRecord.setToolId(toolId);
        installRecord.setUserId(userId);
        installRecord.prePersist();
        installRecordMapper.insert(installRecord);
    }

    @Transactional
    public void publishTool(Long toolId) {
        Tool tool = toolMapper.findById(toolId);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }
        tool.setStatus("active");
        tool.preUpdate();
        toolMapper.update(tool);
    }

    @Transactional
    public void unpublishTool(Long toolId) {
        Tool tool = toolMapper.findById(toolId);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }
        tool.setStatus("inactive");
        tool.preUpdate();
        toolMapper.update(tool);
    }
}
