package com.devtoolmp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentReplyRequest {
    @NotBlank(message = "回复内容不能为空")
    private String content;

    private String username;
}
