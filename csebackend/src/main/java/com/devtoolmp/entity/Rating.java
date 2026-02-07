package com.devtoolmp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    private Long id;
    private Long toolId;
    private String userId;
    private String username;
    private Integer score;
    private String comment;
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
}
