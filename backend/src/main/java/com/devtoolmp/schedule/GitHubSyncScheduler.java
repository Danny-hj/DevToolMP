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
     * 每天凌晨2点同步所有工具的GitHub数据
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncGitHubDataDaily() {
        log.info("Starting daily GitHub data sync at {}", LocalDateTime.now());
        try {
            var result = gitHubService.syncAllToolsGitHubData();
            log.info("GitHub data sync completed: {}", result);
        } catch (Exception e) {
            log.error("Error during daily GitHub data sync", e);
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
