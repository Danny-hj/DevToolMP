package com.devtoolmp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ToolCreateRequest {
    @NotBlank(message = "工具名称不能为空")
    private String name;

    private String description;

    private Long categoryId;

    private String githubOwner;

    private String githubRepo;

    private String version;
}
