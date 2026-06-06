package com.justin.modelops.common.health;

import com.justin.modelops.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight public liveness endpoint, distinct from the Actuator health endpoint.
 */
@Tag(name = "Health")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Operation(summary = "Service liveness check")
    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("status", "UP", "service", "modelops-dashboard"));
    }
}
