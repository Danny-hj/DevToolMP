package com.devtoolmp.schedule;

import com.devtoolmp.service.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * GitHub数据同步定时任务
 */
@Component
public class GitHubSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(GitHubSyncScheduler.class);

    @Autowired
    private GitHubService gitHubService;

    /**
     * 每天凌晨2点同步所有工具的GitHub数据并自动发现新的Agent Skills
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncGitHubDataDaily() {
        log.info("Starting daily GitHub data sync and agent skill discovery at {}", LocalDateTime.now());
        try {
            // 先同步已有工具的数据
            var syncResult = gitHubService.syncAllToolsGitHubData();
            log.info("GitHub data sync completed: {}", syncResult);

            // 然后自动发现并创建新的 agent skills
            var discoverResult = gitHubService.autoDiscoverAndCreateAgentSkills();
            log.info("Agent skill auto-discovery completed: {}", discoverResult);
        } catch (Exception e) {
            log.error("Error during daily GitHub data sync and agent skill discovery", e);
        }
    }

    /**
     * 每小时同步一次（可选，根据需要启用）
     */
    // @Scheduled(cron = "0 0 * * * ?")
    public void syncGitHubDataHourly() {
        log.info("Starting hourly GitHub data sync at {}", LocalDateTime.now());
        try {
            var result = gitHubService.syncAllToolsGitHubData();
            log.info("GitHub data sync completed: {}", result);
        } catch (Exception e) {
            log.error("Error during hourly GitHub data sync", e);
        }
    }
}
