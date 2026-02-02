package com.devtoolmp.service;

import com.devtoolmp.entity.Codehub;
import com.devtoolmp.entity.Tool;
import com.devtoolmp.mapper.CodehubMapper;
import com.devtoolmp.mapper.ToolMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 代码仓库集成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodehubService {

    private static final String GITHUB_API_BASE = "https://api.github.com";

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    @Value("${github.api.token:}")
    private String githubApiToken;

    private final CodehubMapper codehubMapper;
    private final ToolMapper toolMapper;
    private final RestTemplate restTemplate;

    public Codehub getCodehubById(Long id) {
        return codehubMapper.findById(id);
    }

    public Codehub getCodehubByOwnerAndRepo(String owner, String repo) {
        return codehubMapper.findByOwnerAndRepo(owner, repo);
    }

    public List<Codehub> getAllCodehubs() {
        return codehubMapper.findAll();
    }

    @Transactional
    public Codehub createCodehub(Codehub codehub) {
        codehub.prePersist();
        codehubMapper.insert(codehub);
        return codehub;
    }

    @Transactional
    public Codehub updateCodehub(Long id, Codehub codehub) {
        Codehub existing = getCodehubById(id);
        if (existing == null) {
            throw new RuntimeException("Codehub not found: " + id);
        }
        codehub.setId(id);
        codehub.preUpdate();
        codehubMapper.update(codehub);
        return codehub;
    }

    @Transactional
    public void deleteCodehub(Long id) {
        codehubMapper.deleteById(id);
    }

    public int countAll() {
        return codehubMapper.countAll();
    }

    /**
     * 根据仓库信息获取或创建 Codehub 记录
     */
    @Transactional
    public Codehub getOrCreateCodehub(String owner, String repo, String version,
                                        Integer stars, Integer forks, Integer openIssues, Integer watchers) {
        Codehub codehub = getCodehubByOwnerAndRepo(owner, repo);
        if (codehub == null) {
            codehub = new Codehub();
            codehub.setOwner(owner);
            codehub.setRepo(repo);
            codehub.setVersion(version);
            codehub.setStars(stars != null ? stars : 0);
            codehub.setForks(forks != null ? forks : 0);
            codehub.setOpenIssues(openIssues != null ? openIssues : 0);
            codehub.setWatchers(watchers != null ? watchers : 0);
            codehub.prePersist();
            codehubMapper.insert(codehub);
            log.info("创建新的 Codehub 记录: {}/{}", owner, repo);
        } else {
            // 更新统计信息
            if (version != null) {
                codehub.setVersion(version);
            }
            if (stars != null) {
                codehub.setStars(stars);
            }
            if (forks != null) {
                codehub.setForks(forks);
            }
            if (openIssues != null) {
                codehub.setOpenIssues(openIssues);
            }
            if (watchers != null) {
                codehub.setWatchers(watchers);
            }
            codehub.preUpdate();
            codehubMapper.update(codehub);
            log.info("更新 Codehub 记录: {}/{}", owner, repo);
        }
        return codehub;
    }

    /**
     * 从GitHub API获取仓库信息
     */
    @Cacheable(value = "githubRepoInfo", key = "#owner + '/' + #repo", unless = "#result == null")
    public Map<String, Object> fetchRepositoryInfo(String owner, String repo) {
        try {
            String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github.v3+json");

            // 如果配置了GitHub token，则添加到请求头
            if (githubApiToken != null && !githubApiToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + githubApiToken);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            log.warn("Failed to fetch GitHub repo info for {}/{}: {}", owner, repo, response.getStatusCode());
            return null;
        } catch (Exception e) {
            log.error("Error fetching GitHub repo info for {}/{}", owner, repo, e);
            return null;
        }
    }

    /**
     * 同步代码仓库数据到本地数据库
     */
    @Transactional
    public Tool syncCodehubData(Long toolId) {
        Tool tool = toolMapper.findById(toolId);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }

        if (tool.getCodehubId() == null) {
            log.warn("Tool {} has no codehub information", toolId);
            return tool;
        }

        Codehub codehub = codehubMapper.findById(tool.getCodehubId());
        if (codehub == null) {
            log.warn("Codehub not found for tool {}", toolId);
            return tool;
        }

        Map<String, Object> repoInfo = fetchRepositoryInfo(codehub.getOwner(), codehub.getRepo());
        if (repoInfo == null) {
            log.warn("Failed to fetch GitHub info for tool {}", toolId);
            return tool;
        }

        // 更新codehub的统计数据
        Integer stars = getIntegerValue(repoInfo, "stargazers_count");
        Integer forks = getIntegerValue(repoInfo, "forks_count");
        Integer openIssues = getIntegerValue(repoInfo, "open_issues_count");
        Integer watchers = getIntegerValue(repoInfo, "watchers_count");

        codehub.setStars(stars != null ? stars : 0);
        codehub.setForks(forks != null ? forks : 0);
        codehub.setOpenIssues(openIssues != null ? openIssues : 0);
        codehub.setWatchers(watchers != null ? watchers : 0);
        codehub.preUpdate();
        codehubMapper.update(codehub);

        log.info("Successfully synced codehub data for tool {}: stars={}, forks={}, issues={}",
                toolId, stars, forks, openIssues);

        return tool;
    }

    /**
     * 批量同步所有工具的代码仓库数据
     */
    @Transactional
    public Map<String, Object> syncAllToolsCodehubData() {
        int successCount = 0;
        int failureCount = 0;

        // 这里只同步活跃的工具
        var tools = toolMapper.findByStatus("active");

        for (Tool tool : tools) {
            try {
                syncCodehubData(tool.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to sync codehub data for tool {}", tool.getId(), e);
                failureCount++;
            }

            // 添加延迟以避免GitHub API速率限制
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", tools.size());
        result.put("success", successCount);
        result.put("failure", failureCount);
        result.put("timestamp", LocalDateTime.now());

        return result;
    }

    /**
     * 获取README内容
     */
    @Cacheable(value = "githubReadme", key = "#owner + '/' + #repo", unless = "#result == null")
    public String fetchReadme(String owner, String repo) {
        try {
            String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/readme";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github.v3+json");

            if (githubApiToken != null && !githubApiToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + githubApiToken);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // GitHub API返回的README是base64编码的
                String content = (String) response.getBody().get("content");
                if (content != null) {
                    // 解码base64内容（GitHub使用base64编码）
                    return new String(java.util.Base64.getDecoder().decode(content.replace("\n", "")));
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error fetching README for {}/{}", owner, repo, e);
            return null;
        }
    }

    /**
     * 获取最新发布版本
     */
    @Cacheable(value = "githubLatestRelease", key = "#owner + '/' + #repo", unless = "#result == null")
    public Map<String, Object> fetchLatestRelease(String owner, String repo) {
        try {
            String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/releases/latest";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github.v3+json");

            if (githubApiToken != null && !githubApiToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + githubApiToken);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            return null;
        } catch (Exception e) {
            log.error("Error fetching latest release for {}/{}", owner, repo, e);
            return null;
        }
    }

    /**
     * 验证代码仓库是否存在
     */
    public boolean isValidRepository(String owner, String repo) {
        try {
            Map<String, Object> repoInfo = fetchRepositoryInfo(owner, repo);
            return repoInfo != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证仓库是否是 Agent Skill 并返回详细信息
     */
    public Map<String, Object> validateAgentSkill(String owner, String repo) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("owner", owner);
        result.put("repo", repo);
        return result;
    }

    /**
     * 辅助方法：从Map中安全地获取Integer值
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 搜索 GitHub 上的 Agent Skills 仓库
     * 根据配置的 topics 搜索符合条件的仓库
     */
    public List<Map<String, Object>> searchAgentSkillRepos() {
        List<Map<String, Object>> allResults = new ArrayList<>();
        String[] topics = {"agent-skill", "claude-skill", "skill", "skills"};

        for (String topic : topics) {
            try {
                String query = "topic:" + topic;
                String url = GITHUB_API_BASE + "/search/repositories?q=" + query
                        + "&sort=stars&order=desc&per_page=30";

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/vnd.github.v3+json");

                if (githubApiToken != null && !githubApiToken.isEmpty()) {
                    headers.set("Authorization", "Bearer " + githubApiToken);
                }

                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
                    if (items != null) {
                        allResults.addAll(items);
                        log.info("Found {} repositories for topic: {}", items.size(), topic);
                    }
                }

                // 添加延迟以避免GitHub API速率限制
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("Error searching for agent skills with topic: {}", topic, e);
            }
        }

        // 去重（根据 full_name）
        Map<String, Map<String, Object>> uniqueRepos = new LinkedHashMap<>();
        for (Map<String, Object> repo : allResults) {
            String fullName = (String) repo.get("full_name");
            if (fullName != null && !uniqueRepos.containsKey(fullName)) {
                uniqueRepos.put(fullName, repo);
            }
        }

        List<Map<String, Object>> results = new ArrayList<>(uniqueRepos.values());
        log.info("Total unique agent skill repositories found: {}", results.size());
        return results;
    }

    /**
     * 自动发现并创建 Agent Skills 工具
     * 搜索 GitHub 上的 agent skills 仓库，如果不存在则自动创建
     */
    @Transactional
    public Map<String, Object> autoDiscoverAndCreateAgentSkills() {
        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        try {
            // 搜索 agent skills 仓库
            List<Map<String, Object>> repos = searchAgentSkillRepos();
            log.info("Found {} candidate agent skill repositories", repos.size());

            for (Map<String, Object> repo : repos) {
                try {
                    String fullName = (String) repo.get("full_name");
                    String[] parts = fullName.split("/");
                    if (parts.length != 2) {
                        log.warn("Invalid repository name: {}", fullName);
                        skippedCount++;
                        continue;
                    }

                    String owner = parts[0];
                    String repoName = parts[1];

                    // 检查 codehub 是否已存在
                    Codehub existingCodehub = codehubMapper.findByOwnerAndRepo(owner, repoName);
                    if (existingCodehub != null) {
                        log.info("Codehub already exists: {}, updating data", fullName);
                        // 查找使用该 codehub 的工具并同步数据
                        Tool tool = toolMapper.findByCodehubId(existingCodehub.getId());
                        if (tool != null) {
                            syncCodehubData(tool.getId());
                        }
                        updatedCount++;
                    } else {
                        // 创建新工具和 codehub
                        Tool newTool = createToolFromGitHubRepo(repo);
                        if (newTool != null) {
                            log.info("Created new tool from repository: {}", fullName);
                            createdCount++;
                        } else {
                            log.warn("Failed to create tool from repository: {}", fullName);
                            errorCount++;
                        }
                    }

                    // 添加延迟以避免数据库压力
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("Error processing repository", e);
                    errorCount++;
                    errors.add(e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", repos.size());
            result.put("created", createdCount);
            result.put("updated", updatedCount);
            result.put("skipped", skippedCount);
            result.put("errors", errorCount);
            result.put("errorDetails", errors);
            result.put("timestamp", LocalDateTime.now());

            log.info("Auto-discovery completed: created={}, updated={}, skipped={}, errors={}",
                    createdCount, updatedCount, skippedCount, errorCount);

            return result;
        } catch (Exception e) {
            log.error("Error during auto-discovery of agent skills", e);
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
            return result;
        }
    }

    /**
     * 从 GitHub 仓库信息创建工具和 codehub 对象
     */
    private Tool createToolFromGitHubRepo(Map<String, Object> repo) {
        try {
            String fullName = (String) repo.get("full_name");
            String[] parts = fullName.split("/");
            String owner = parts[0];
            String repoName = parts[1];

            // 创建 codehub 记录
            Integer stars = getIntegerValue(repo, "stargazers_count");
            Integer forks = getIntegerValue(repo, "forks_count");
            Integer openIssues = getIntegerValue(repo, "open_issues_count");
            Integer watchers = getIntegerValue(repo, "watchers_count");

            Codehub codehub = new Codehub();
            codehub.setOwner(owner);
            codehub.setRepo(repoName);
            codehub.setVersion("latest");
            codehub.setStars(stars != null ? stars : 0);
            codehub.setForks(forks != null ? forks : 0);
            codehub.setOpenIssues(openIssues != null ? openIssues : 0);
            codehub.setWatchers(watchers != null ? watchers : 0);
            codehub.prePersist();
            codehubMapper.insert(codehub);

            // 创建 tool 记录
            Tool tool = new Tool();
            tool.setName((String) repo.get("name"));
            tool.setDescription((String) repo.get("description"));
            tool.setCategoryId(1L); // 默认分类ID
            tool.setCodehubId(codehub.getId());
            tool.setStatus("active");
            tool.prePersist();
            toolMapper.insert(tool);

            return tool;
        } catch (Exception e) {
            log.error("Error creating tool from GitHub repo", e);
            return null;
        }
    }
}
