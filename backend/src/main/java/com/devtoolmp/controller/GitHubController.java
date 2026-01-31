package com.devtoolmp.controller;

import com.devtoolmp.dto.response.ToolDTO;
import com.devtoolmp.entity.Tool;
import com.devtoolmp.service.GitHubService;
import com.devtoolmp.service.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * GitHub集成控制器
 */
@RestController
@RequestMapping("/github")
public class GitHubController {

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private ToolService toolService;

    /**
     * 获取GitHub仓库信息
     */
    @GetMapping("/repos/{owner}/{repo}")
    public ResponseEntity<Map<String, Object>> getRepositoryInfo(
            @PathVariable String owner,
            @PathVariable String repo) {
        Map<String, Object> repoInfo = gitHubService.fetchRepositoryInfo(owner, repo);
        if (repoInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repoInfo);
    }

    /**
     * 同步单个工具的GitHub数据
     */
    @PostMapping("/sync/{toolId}")
    public ResponseEntity<ToolDTO> syncToolGitHubData(@PathVariable Long toolId) {
        try {
            Tool tool = gitHubService.syncGitHubData(toolId);
            ToolDTO toolDTO = toolService.getToolById(toolId);
            return ResponseEntity.ok(toolDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量同步所有工具的GitHub数据
     */
    @PostMapping("/sync/all")
    public ResponseEntity<Map<String, Object>> syncAllToolsGitHubData() {
        Map<String, Object> result = gitHubService.syncAllToolsGitHubData();
        return ResponseEntity.ok(result);
    }

    /**
     * 获取仓库的README内容
     */
    @GetMapping("/repos/{owner}/{repo}/readme")
    public ResponseEntity<Map<String, String>> getReadme(
            @PathVariable String owner,
            @PathVariable String repo) {
        String readme = gitHubService.fetchReadme(owner, repo);
        if (readme == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "owner", owner,
                "repo", repo,
                "content", readme
        ));
    }

    /**
     * 获取仓库的最新发布版本
     */
    @GetMapping("/repos/{owner}/{repo}/releases/latest")
    public ResponseEntity<Map<String, Object>> getLatestRelease(
            @PathVariable String owner,
            @PathVariable String repo) {
        Map<String, Object> release = gitHubService.fetchLatestRelease(owner, repo);
        if (release == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(release);
    }

    /**
     * 验证GitHub仓库是否存在
     */
    @GetMapping("/repos/{owner}/{repo}/validate")
    public ResponseEntity<Map<String, Boolean>> validateRepository(
            @PathVariable String owner,
            @PathVariable String repo) {
        boolean isValid = gitHubService.isValidRepository(owner, repo);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
