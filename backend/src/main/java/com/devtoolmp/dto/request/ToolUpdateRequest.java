package com.devtoolmp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ToolUpdateRequest {
    private String name;

    private String description;

    private Long categoryId;

    private String githubOwner;

    private String githubRepo;

    private String version;
}
