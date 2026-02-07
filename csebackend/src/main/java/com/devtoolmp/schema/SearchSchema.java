package com.devtoolmp.schema;

import com.devtoolmp.dto.response.ToolDTO;
import com.devtoolmp.dto.response.PageResponse;
import com.devtoolmp.service.ToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索 REST Schema
 *
 * 模拟 ServiceComb 的 @RestSchema 功能
 * 使用标准 Spring Boot 注解实现
 */
@Slf4j
@RestController
@RequestMapping("/webapi/toolmarket/v1/search")
public class SearchSchema {

    @Autowired
    private ToolService toolService;

    @GetMapping
    public ResponseEntity<PageResponse<ToolDTO>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ToolDTO> results = toolService.searchTools(q, page, size);
        return ResponseEntity.ok(results);
    }
}
