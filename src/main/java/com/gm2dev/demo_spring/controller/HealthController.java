package com.gm2dev.demo_spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public")
@Tag(name = "Public Health", description = "Public health check endpoints")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Simple health check endpoint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "demo-spring");
        health.put("version", "1.0.0");

        return ResponseEntity.ok(health);
    }

    @GetMapping("/info")
    @Operation(summary = "Service info", description = "Get service information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service information retrieved")
    })
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Demo Spring Microservice");
        info.put("description", "Spring Boot microservice for demo purposes");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(info);
    }
}