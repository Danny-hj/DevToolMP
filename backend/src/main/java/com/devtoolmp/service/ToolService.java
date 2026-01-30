package com.devtoolmp.service;

import com.devtoolmp.dto.request.ToolCreateRequest;
import com.devtoolmp.dto.request.ToolUpdateRequest;
import com.devtoolmp.dto.response.*;
import com.devtoolmp.entity.*;
import com.devtoolmp.repository.*;
import com.devtoolmp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ToolService {

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ViewRecordRepository viewRecordRepository;

    @Autowired
    private TelemetryRepository telemetryRepository;

    @Autowired
    private ToolTagRepository toolTagRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CommentReplyRepository commentReplyRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Tool createTool(ToolCreateRequest request) {
        Tool tool = new Tool();
        tool.setName(request.getName());
        tool.setDescription(request.getDescription());
        tool.setCategoryId(request.getCategoryId());
        tool.setGithubOwner(request.getGithubOwner());
        tool.setGithubRepo(request.getGithubRepo());
        tool.setVersion(request.getVersion());
        return toolRepository.save(tool);
    }

    @Transactional
    public Tool updateTool(Long id, ToolUpdateRequest request) {
        Optional<Tool> toolOptional = toolRepository.findById(id);
        if (toolOptional.isEmpty()) {
            throw new RuntimeException("Tool not found");
        }
        Tool tool = toolOptional.get();
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
        return toolRepository.save(tool);
    }

    @Transactional
    public void deleteTool(Long id) {
        toolRepository.deleteById(id);
    }

    public ToolDTO getToolById(Long id) {
        Optional<Tool> toolOptional = toolRepository.findById(id);
        if (toolOptional.isEmpty()) {
            throw new RuntimeException("Tool not found");
        }
        Tool tool = toolOptional.get();
        String categoryName = tool.getCategoryId() != null ?
                categoryRepository.findById(tool.getCategoryId()).map(Category::getName).orElse(null) : null;
        List<ToolTag> toolTags = toolTagRepository.findByToolId(id);
        List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
        return ToolDTO.fromEntity(tool, categoryName, tags);
    }

    public ToolDetailDTO getToolDetailById(Long id, Long userId) {
        Optional<Tool> toolOptional = toolRepository.findById(id);
        if (toolOptional.isEmpty()) {
            throw new RuntimeException("Tool not found");
        }
        Tool tool = toolOptional.get();
        String categoryName = tool.getCategoryId() != null ?
                categoryRepository.findById(tool.getCategoryId()).map(Category::getName).orElse(null) : null;
        List<ToolTag> toolTags = toolTagRepository.findByToolId(id);
        List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());

        Double averageRating = ratingRepository.getAverageScoreByToolId(id);
        Long totalRatings = ratingRepository.countByToolId(id);

        boolean isFavorited = false;
        if (userId != null) {
            isFavorited = favoriteRepository.existsByUserIdAndToolId(userId, id);
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

    public Page<ToolDTO> getTools(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Tool> tools = toolRepository.findByStatus("active", pageable);
        return tools.map(tool -> {
            String categoryName = tool.getCategoryId() != null ?
                    categoryRepository.findById(tool.getCategoryId()).map(Category::getName).orElse(null) : null;
            List<ToolTag> toolTags = toolTagRepository.findByToolId(tool.getId());
            List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
            return ToolDTO.fromEntity(tool, categoryName, tags);
        });
    }

    public Page<ToolDTO> searchTools(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("hotScoreDaily").descending());
        Page<Tool> tools = toolRepository.searchByKeyword(keyword, pageable);
        return tools.map(tool -> {
            String categoryName = tool.getCategoryId() != null ?
                    categoryRepository.findById(tool.getCategoryId()).map(Category::getName).orElse(null) : null;
            List<ToolTag> toolTags = toolTagRepository.findByToolId(tool.getId());
            List<String> tags = toolTags.stream().map(ToolTag::getTagName).collect(Collectors.toList());
            return ToolDTO.fromEntity(tool, categoryName, tags);
        });
    }

    @Transactional
    public void recordView(Long toolId, Long userId, String ipAddress, String userAgent) {
        Tool tool = toolRepository.findById(toolId).orElseThrow(() -> new RuntimeException("Tool not found"));
        tool.setViewCount(tool.getViewCount() + 1);
        toolRepository.save(tool);

        ViewRecord viewRecord = new ViewRecord();
        viewRecord.setToolId(toolId);
        viewRecord.setUserId(userId);
        viewRecord.setIpAddress(ipAddress);
        viewRecord.setUserAgent(userAgent);
        viewRecordRepository.save(viewRecord);
    }

    @Transactional
    public boolean toggleFavorite(Long toolId, Long userId) {
        Optional<Favorite> favoriteOptional = favoriteRepository.findByUserIdAndToolId(userId, toolId);
        Tool tool = toolRepository.findById(toolId).orElseThrow(() -> new RuntimeException("Tool not found"));

        if (favoriteOptional.isPresent()) {
            favoriteRepository.delete(favoriteOptional.get());
            tool.setFavoriteCount(tool.getFavoriteCount() - 1);
            toolRepository.save(tool);
            return false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setToolId(toolId);
            favoriteRepository.save(favorite);
            tool.setFavoriteCount(tool.getFavoriteCount() + 1);
            toolRepository.save(tool);
            return true;
        }
    }

    public boolean isFavorited(Long toolId, Long userId) {
        return favoriteRepository.existsByUserIdAndToolId(userId, toolId);
    }

    @Transactional
    public void recordInstall(Long toolId, String ipAddress, String userAgent) {
        Tool tool = toolRepository.findById(toolId).orElseThrow(() -> new RuntimeException("Tool not found"));
        tool.setInstallCount(tool.getInstallCount() + 1);
        toolRepository.save(tool);

        TelemetryData telemetryData = new TelemetryData();
        telemetryData.setToolId(toolId);
        telemetryData.setEventType("install");
        telemetryData.setIpAddress(ipAddress);
        telemetryData.setUserAgent(userAgent);
        telemetryRepository.save(telemetryData);
    }
}
