package com.devtoolmp.schema;

import com.devtoolmp.dto.response.ToolDTO;
import com.devtoolmp.entity.Tool;
import com.devtoolmp.service.CodehubService;
import com.devtoolmp.service.ToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 代码仓库集成 REST Schema
 *
 * 模拟 ServiceComb 的 @RestSchema 功能
 * 使用标准 Spring Boot 注解实现
 */
@Slf4j
@RestController
@RequestMapping("/webapi/toolmarket/v1/codehub")
public class CodehubSchema {

    @Autowired
    private CodehubService codehubService;

    @Autowired
    private ToolService toolService;

    /**
     * 获取代码仓库信息
     */
    @GetMapping("/repos/{owner}/{repo}")
    public ResponseEntity<Map<String, Object>> getRepositoryInfo(
            @PathVariable String owner,
            @PathVariable String repo) {
        Map<String, Object> repoInfo = codehubService.fetchRepositoryInfo(owner, repo);
        if (repoInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repoInfo);
    }

    /**
     * 同步单个工具的代码仓库数据
     */
    @PostMapping("/sync/{toolId}")
    public ResponseEntity<ToolDTO> syncToolCodehubData(@PathVariable Long toolId) {
        try {
            Tool tool = codehubService.syncCodehubData(toolId);
            ToolDTO toolDTO = toolService.getToolById(toolId);
            return ResponseEntity.ok(toolDTO);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量同步所有工具的代码仓库数据
     */
    @PostMapping("/sync/all")
    public ResponseEntity<Map<String, Object>> syncAllToolsCodehubData() {
        Map<String, Object> result = codehubService.syncAllToolsCodehubData();
        return ResponseEntity.ok(result);
    }

    /**
     * 获取仓库的README内容
     */
    @GetMapping("/repos/{owner}/{repo}/readme")
    public ResponseEntity<Map<String, String>> getReadme(
            @PathVariable String owner,
            @PathVariable String repo) {
        String readme = codehubService.fetchReadme(owner, repo);
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
        Map<String, Object> release = codehubService.fetchLatestRelease(owner, repo);
        if (release == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(release);
    }

    /**
     * 验证代码仓库是否存在
     */
    @GetMapping("/repos/{owner}/{repo}/validate")
    public ResponseEntity<Map<String, Boolean>> validateRepository(
            @PathVariable String owner,
            @PathVariable String repo) {
        boolean isValid = codehubService.isValidRepository(owner, repo);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    /**
     * 验证仓库是否是 Agent Skill
     */
    @GetMapping("/repos/{owner}/{repo}/validate-agent-skill")
    public ResponseEntity<Map<String, Object>> validateAgentSkill(
            @PathVariable String owner,
            @PathVariable String repo) {
        Map<String, Object> result = codehubService.validateAgentSkill(owner, repo);
        return ResponseEntity.ok(result);
    }

    /**
     * 自动发现并创建 Agent Skills 工具（手动触发）
     */
    @PostMapping("/agent-skills/auto-discover")
    public ResponseEntity<Map<String, Object>> autoDiscoverAgentSkills() {
        Map<String, Object> result = codehubService.autoDiscoverAndCreateAgentSkills();
        return ResponseEntity.ok(result);
    }

    /**
     * 搜索 Agent Skills 仓库
     */
    @GetMapping("/agent-skills/search")
    public ResponseEntity<List<Map<String, Object>>> searchAgentSkills() {
        List<Map<String, Object>> results = codehubService.searchAgentSkillRepos();
        return ResponseEntity.ok(results);
    }

    /**
     * 获取所有代码仓库列表
     */
    @GetMapping
    public ResponseEntity<List<com.devtoolmp.entity.Codehub>> getAllCodehubs() {
        List<com.devtoolmp.entity.Codehub> codehubs = codehubService.getAllCodehubs();
        return ResponseEntity.ok(codehubs);
    }

    /**
     * 根据ID获取代码仓库
     */
    @GetMapping("/{id}")
    public ResponseEntity<com.devtoolmp.entity.Codehub> getCodehubById(@PathVariable Long id) {
        com.devtoolmp.entity.Codehub codehub = codehubService.getCodehubById(id);
        if (codehub == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(codehub);
    }
}
