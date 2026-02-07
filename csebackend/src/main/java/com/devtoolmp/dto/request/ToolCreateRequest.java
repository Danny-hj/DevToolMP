package com.devtoolmp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ToolCreateRequest {
    @NotBlank(message = "工具名称不能为空")
    @Size(min = 2, max = 100, message = "工具名称长度必须在2-100个字符之间")
    private String name;

    @NotBlank(message = "工具描述不能为空")
    @Size(min = 10, max = 500, message = "工具描述长度必须在10-500个字符之间")
    private String description;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private String githubOwner;

    private String githubRepo;

    private String version;

    private List<String> tags;

    private String status = "active";
}
