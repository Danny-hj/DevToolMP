package com.devtoolmp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Codehub {
    private Long id;
    private String owner;
    private String repo;
    private String version;
    private Integer stars = 0;
    private Integer forks = 0;
    private Integer openIssues = 0;
    private Integer watchers = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        updateTime = now;
    }

    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    public String getUrl() {
        if (owner != null && !owner.trim().isEmpty()
                && repo != null && !repo.trim().isEmpty()) {
            return "https://github.com/" + owner + "/" + repo;
        }
        return null;
    }
}
