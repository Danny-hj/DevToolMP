package com.devtoolmp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentReplyDTO {
    private Long id;
    private Long ratingId;
    private Long userId;
    private String username;
    private String userAvatar;
    private Long replyToUserId;
    private String replyToUsername;
    private String content;
    private LocalDateTime createTime;
}
