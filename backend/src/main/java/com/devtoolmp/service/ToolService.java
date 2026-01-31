package com.devtoolmp.service;

import com.devtoolmp.dto.request.ToolCreateRequest;
import com.devtoolmp.dto.request.ToolUpdateRequest;
import com.devtoolmp.dto.response.*;
import com.devtoolmp.entity.*;
import com.devtoolmp.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToolService {

    @Autowired
    private ToolMapper toolMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ViewRecordMapper viewRecordMapper;

    @Autowired
    private TelemetryDataMapper telemetryDataMapper;

    @Autowired
    private ToolTagMapper toolTagMapper;

    @Autowired
    private RatingMapper ratingMapper;

    @Transactional
    public Tool createTool(ToolCreateRequest request) {
        Tool tool = new Tool();
        tool.setName(request.getName());
        tool.setDescription(request.getDescription());
        tool.setCategoryId(request.getCategoryId());
        tool.setGithubOwner(request.getGithubOwner());
        tool.setGithubRepo(request.getGithubRepo());
        tool.setVersion(request.getVersion());
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
    public Tool updateTool(Long id, ToolUpdateRequest request) {
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
        if (request.getGithubOwner() != null) {
            tool.setGithubOwner(request.getGithubOwner());
        }
        if (request.getGithubRepo() != null) {
            tool.setGithubRepo(request.getGithubRepo());
        }
        if (request.getVersion() != null) {
            tool.setVersion(request.getVersion());
        }
        tool.preUpdate();
        toolMapper.update(tool);
        return tool;
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
        List<ToolTag> toolTags = toolTagMapper.findByToolId(id);
        List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
        return ToolDTO.fromEntity(tool, categoryName, tags);
    }

    public ToolDetailDTO getToolDetailById(Long id, String clientIdentifier) {
        Tool tool = toolMapper.findById(id);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }
        Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
        String categoryName = category != null ? category.getName() : null;
        List<ToolTag> toolTags = toolTagMapper.findByToolId(id);
        List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());

        Double averageRating = ratingMapper.getAverageScoreByToolId(id);
        Long totalRatings = (long) ratingMapper.countByToolId(id);

        boolean isFavorited = false;
        if (clientIdentifier != null) {
            isFavorited = favoriteMapper.existsByClientIdentifierAndToolId(clientIdentifier, id);
        }

        ToolDetailDTO dto = new ToolDetailDTO();
        dto.setId(tool.getId());
        dto.setName(tool.getName());
        dto.setDescription(tool.getDescription());
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
        dto.setStatus(tool.getStatus());
        dto.setTags(tags);
        dto.setAverageRating(averageRating);
        dto.setTotalRatings(totalRatings != null ? totalRatings.intValue() : 0);
        dto.setFavorited(isFavorited);
        dto.setUpdatedAt(tool.getUpdatedAt());
        dto.setCreatedAt(tool.getCreatedAt());
        return dto;
    }

    public PageResponse<ToolDTO> getTools(int page, int size) {
        int offset = page * size;
        List<Tool> tools = toolMapper.findByStatusWithPage("active", offset, size);
        int total = toolMapper.countByStatus("active");

        List<ToolDTO> toolDTOs = tools.stream().map(tool -> {
            Category category = tool.getCategoryId() != null ? categoryMapper.findById(tool.getCategoryId()) : null;
            String categoryName = category != null ? category.getName() : null;
            List<ToolTag> toolTags = toolTagMapper.findByToolId(tool.getId());
            List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
            return ToolDTO.fromEntity(tool, categoryName, tags);
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
            List<ToolTag> toolTags = toolTagMapper.findByToolId(tool.getId());
            List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
            return ToolDTO.fromEntity(tool, categoryName, tags);
        }).collect(Collectors.toList());

        return PageResponse.of(toolDTOs, page, size, (long) total);
    }

    @Transactional
    public void recordView(Long toolId, String clientIdentifier, String ipAddress, String userAgent) {
        Tool tool = toolMapper.findById(toolId);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }
        tool.setViewCount(tool.getViewCount() + 1);
        tool.preUpdate();
        toolMapper.update(tool);

        ViewRecord viewRecord = new ViewRecord();
        viewRecord.setToolId(toolId);
        viewRecord.setClientIdentifier(clientIdentifier);
        viewRecord.setIpAddress(ipAddress);
        viewRecord.setUserAgent(userAgent);
        viewRecord.prePersist();
        viewRecordMapper.insert(viewRecord);
    }

    @Transactional
    public boolean toggleFavorite(Long toolId, String clientIdentifier) {
        Favorite favorite = favoriteMapper.findByClientIdentifierAndToolId(clientIdentifier, toolId);
        Tool tool = toolMapper.findById(toolId);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }

        if (favorite != null) {
            favoriteMapper.deleteByClientIdentifierAndToolId(clientIdentifier, toolId);
            tool.setFavoriteCount(tool.getFavoriteCount() - 1);
            tool.preUpdate();
            toolMapper.update(tool);
            return false;
        } else {
            favorite = new Favorite();
            favorite.setClientIdentifier(clientIdentifier);
            favorite.setToolId(toolId);
            favorite.prePersist();
            favoriteMapper.insert(favorite);
            tool.setFavoriteCount(tool.getFavoriteCount() + 1);
            tool.preUpdate();
            toolMapper.update(tool);
            return true;
        }
    }

    public boolean isFavorited(Long toolId, String clientIdentifier) {
        return favoriteMapper.existsByClientIdentifierAndToolId(clientIdentifier, toolId);
    }

    @Transactional
    public void recordInstall(Long toolId, String ipAddress, String userAgent) {
        Tool tool = toolMapper.findById(toolId);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }
        tool.setInstallCount(tool.getInstallCount() + 1);
        tool.preUpdate();
        toolMapper.update(tool);

        TelemetryData telemetryData = new TelemetryData();
        telemetryData.setToolId(toolId);
        telemetryData.setEventType("install");
        telemetryData.setIpAddress(ipAddress);
        telemetryData.setUserAgent(userAgent);
        telemetryData.prePersist();
        telemetryDataMapper.insert(telemetryData);
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
