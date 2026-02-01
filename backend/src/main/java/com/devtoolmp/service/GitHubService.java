package com.devtoolmp.service;

import com.devtoolmp.entity.Tool;
import com.devtoolmp.mapper.ToolMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * GitHub集成服务
 */
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);
    private static final String GITHUB_API_BASE = "https://api.github.com";

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    @Value("${github.api.token:}")
    private String githubApiToken;

    @Autowired
    private ToolMapper toolMapper;

    @Autowired
    private RestTemplate restTemplate;

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
     * 同步GitHub数据到本地数据库
     */
    @Transactional
    public Tool syncGitHubData(Long toolId) {
        Tool tool = toolMapper.findById(toolId);
        if (tool == null) {
            throw new RuntimeException("Tool not found");
        }

        if (tool.getGithubOwner() == null || tool.getGithubRepo() == null) {
            log.warn("Tool {} has no GitHub information", toolId);
            return tool;
        }

        Map<String, Object> repoInfo = fetchRepositoryInfo(tool.getGithubOwner(), tool.getGithubRepo());
        if (repoInfo == null) {
            log.warn("Failed to fetch GitHub info for tool {}", toolId);
            return tool;
        }

        // 更新工具的GitHub统计数据
        Integer stars = getIntegerValue(repoInfo, "stargazers_count");
        Integer forks = getIntegerValue(repoInfo, "forks_count");
        Integer openIssues = getIntegerValue(repoInfo, "open_issues_count");
        Integer watchers = getIntegerValue(repoInfo, "watchers_count");
        String description = (String) repoInfo.get("description");
        String homepage = (String) repoInfo.get("homepage");

        tool.setStars(stars != null ? stars : 0);
        tool.setForks(forks != null ? forks : 0);
        tool.setOpenIssues(openIssues != null ? openIssues : 0);
        tool.setWatchers(watchers != null ? watchers : 0);

        // 更新描述（如果为空或GitHub有更新的描述）
        if (description != null && !description.isEmpty()) {
            if (tool.getDescription() == null || tool.getDescription().isEmpty()) {
                tool.setDescription(description);
            }
        }

        tool.preUpdate();
        toolMapper.update(tool);

        log.info("Successfully synced GitHub data for tool {}: stars={}, forks={}, issues={}",
                toolId, stars, forks, openIssues);

        return tool;
    }

    /**
     * 批量同步所有工具的GitHub数据
     */
    @Transactional
    public Map<String, Object> syncAllToolsGitHubData() {
        int successCount = 0;
        int failureCount = 0;

        // 这里只同步活跃的工具
        // 如果工具数量很多，建议使用分页处理
        var tools = toolMapper.findByStatus("active");

        for (Tool tool : tools) {
            try {
                syncGitHubData(tool.getId());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to sync GitHub data for tool {}", tool.getId(), e);
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
     * 获取 skill.md 文件内容
     */
    @Cacheable(value = "githubSkillMd", key = "#owner + '/' + #repo", unless = "#result == null")
    public String fetchSkillMd(String owner, String repo) {
        try {
            // 尝试获取 skill.md
            String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/contents/skill.md";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github.v3+json");

            if (githubApiToken != null && !githubApiToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + githubApiToken);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // GitHub API返回的文件内容是base64编码的
                String content = (String) response.getBody().get("content");
                if (content != null) {
                    // 解码base64内容（GitHub使用base64编码）
                    return new String(java.util.Base64.getDecoder().decode(content.replace("\n", "")));
                }
            }

            return null;
        } catch (Exception e) {
            log.debug("skill.md not found for {}/{} (this is expected for many repos)", owner, repo);
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
     * 验证GitHub仓库是否存在
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
        String[] topics = {"agent-skill", "claude-skill", "skill"};

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

                    // 检查工具是否已存在
                    Tool existingTool = toolMapper.findByGithubOwnerAndGithubRepo(owner, repoName);
                    if (existingTool != null) {
                        log.info("Tool already exists: {}, updating GitHub data", fullName);
                        syncGitHubData(existingTool.getId());
                        updatedCount++;
                    } else {
                        // 创建新工具
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
     * 从 GitHub 仓库信息创建工具对象
     */
    private Tool createToolFromGitHubRepo(Map<String, Object> repo) {
        try {
            String fullName = (String) repo.get("full_name");
            String[] parts = fullName.split("/");
            String owner = parts[0];
            String repoName = parts[1];

            // 检查是否已存在
            Tool existing = toolMapper.findByGithubOwnerAndGithubRepo(owner, repoName);
            if (existing != null) {
                return existing;
            }

            Tool tool = new Tool();
            tool.setName((String) repo.get("name"));

            // 优先使用 skill.md 内容作为描述
            String skillMdContent = fetchSkillMd(owner, repoName);
            if (skillMdContent != null && !skillMdContent.isEmpty()) {
                tool.setDescription(skillMdContent);
                log.info("Using skill.md content for repository: {}/{}", owner, repoName);
            } else {
                // 如果没有 skill.md，使用 GitHub 仓库描述
                tool.setDescription((String) repo.get("description"));
                if (tool.getDescription() == null || tool.getDescription().isEmpty()) {
                    tool.setDescription("从 GitHub 自动发现的 Agent Skill 仓库");
                }
            }

            tool.setCategoryId(1L); // 默认分类ID
            tool.setGithubOwner(owner);
            tool.setGithubRepo(repoName);
            tool.setVersion("latest");
            tool.setStatus("active");

            // GitHub 统计数据
            Integer stars = getIntegerValue(repo, "stargazers_count");
            Integer forks = getIntegerValue(repo, "forks_count");
            Integer openIssues = getIntegerValue(repo, "open_issues_count");
            Integer watchers = getIntegerValue(repo, "watchers_count");

            tool.setStars(stars != null ? stars : 0);
            tool.setForks(forks != null ? forks : 0);
            tool.setOpenIssues(openIssues != null ? openIssues : 0);
            tool.setWatchers(watchers != null ? watchers : 0);

            tool.prePersist();
            toolMapper.insert(tool);

            return tool;
        } catch (Exception e) {
            log.error("Error creating tool from GitHub repo", e);
            return null;
        }
    }
}
