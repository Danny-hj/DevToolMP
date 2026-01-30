package com.devtoolmp.controller;

import com.devtoolmp.dto.response.ToolDTO;
import com.devtoolmp.service.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private ToolService toolService;

    @GetMapping
    public ResponseEntity<Page<ToolDTO>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ToolDTO> results = toolService.searchTools(q, page, size);
        return ResponseEntity.ok(results);
    }
}
