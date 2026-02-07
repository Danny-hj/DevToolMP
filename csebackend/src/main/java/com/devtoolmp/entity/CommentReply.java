package com.devtoolmp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentReply {
    private Long id;
    private Long ratingId;
    private String userId;
    private String username;
    private Long replyToUserId;
    private String replyToUsername;
    private String content;
    private LocalDateTime createTime;

    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
