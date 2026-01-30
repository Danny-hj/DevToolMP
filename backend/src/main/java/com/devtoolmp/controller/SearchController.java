package com.devtoolmp.controller;

import com.devtoolmp.dto.response.ToolDTO;
import com.devtoolmp.dto.response.PageResponse;
import com.devtoolmp.service.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class SearchController {

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
