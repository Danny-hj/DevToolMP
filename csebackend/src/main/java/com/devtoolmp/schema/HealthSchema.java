package com.devtoolmp.schema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查 Schema
 */
@Slf4j
@RestController
@RequestMapping("/webapi/toolmarket/v1/health")
public class HealthSchema {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "devtoolmp-csebackend");
        response.put("framework", "Spring Boot 3.2.1");
        response.put("schemaMode", "ServiceComb Simulation");
        response.put("message", "应用运行正常！");
        return ResponseEntity.ok(response);
    }
}
