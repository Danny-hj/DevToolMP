package com.devtoolmp.controller;

import com.devtoolmp.dto.request.ToolCreateRequest;
import com.devtoolmp.dto.request.ToolUpdateRequest;
import com.devtoolmp.dto.response.ToolDTO;
import com.devtoolmp.dto.response.ToolDetailDTO;
import com.devtoolmp.dto.response.PageResponse;
import com.devtoolmp.entity.Tool;
import com.devtoolmp.service.ToolService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tools")
@Validated
public class ToolController {

    @Autowired
    private ToolService toolService;

    @GetMapping
    public ResponseEntity<PageResponse<ToolDTO>> getTools(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ToolDTO> tools = toolService.getTools(page, size);
        return ResponseEntity.ok(tools);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ToolDTO> getTool(@PathVariable Long id) {
        ToolDTO tool = toolService.getToolById(id);
        return ResponseEntity.ok(tool);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ToolDetailDTO> getToolDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        String clientIdentifier = getClientIdentifier(request);

        ToolDetailDTO toolDetail = toolService.getToolDetailById(id, clientIdentifier);
        return ResponseEntity.ok(toolDetail);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ToolDTO>> searchTools(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ToolDTO> tools = toolService.searchTools(keyword, page, size);
        return ResponseEntity.ok(tools);
    }

    @PostMapping
    public ResponseEntity<Object> createTool(
            @Valid @RequestBody ToolCreateRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        Tool tool = toolService.createTool(request);
        ToolDTO toolDTO = toolService.getToolById(tool.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toolDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ToolDTO> updateTool(
            @PathVariable Long id,
            @Valid @RequestBody ToolUpdateRequest request) {
        Tool tool = toolService.updateTool(id, request);
        ToolDTO toolDTO = toolService.getToolById(tool.getId());
        return ResponseEntity.ok(toolDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTool(@PathVariable Long id) {
        toolService.deleteTool(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> recordView(
            @PathVariable Long id,
            HttpServletRequest request) {
        String clientIdentifier = getClientIdentifier(request);
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        toolService.recordView(id, clientIdentifier, ipAddress, userAgent);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Boolean> toggleFavorite(
            @PathVariable Long id,
            HttpServletRequest request) {
        String clientIdentifier = getClientIdentifier(request);

        boolean isFavorited = toolService.toggleFavorite(id, clientIdentifier);
        return ResponseEntity.ok(isFavorited);
    }

    @GetMapping("/{id}/favorite/check")
    public ResponseEntity<Boolean> checkFavorite(
            @PathVariable Long id,
            HttpServletRequest request) {
        String clientIdentifier = getClientIdentifier(request);

        boolean isFavorited = toolService.isFavorited(id, clientIdentifier);
        return ResponseEntity.ok(isFavorited);
    }

    @PostMapping("/{id}/install")
    public ResponseEntity<Void> recordInstall(
            @PathVariable Long id,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        toolService.recordInstall(id, ipAddress, userAgent);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<Void> publishTool(@PathVariable Long id) {
        toolService.publishTool(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublishTool(@PathVariable Long id) {
        toolService.unpublishTool(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 使用IP地址和User-Agent生成客户端标识符
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return ipAddress + ":" + (userAgent != null ? userAgent : "");
    }
}
