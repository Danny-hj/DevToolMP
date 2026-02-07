package com.devtoolmp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tool {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private Long codehubId;
    private String status = "active";
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
