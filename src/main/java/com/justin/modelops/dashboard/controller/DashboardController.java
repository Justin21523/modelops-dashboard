package com.justin.modelops.dashboard.controller;

import com.justin.modelops.common.response.ApiResponse;
import com.justin.modelops.dashboard.dto.DashboardSummaryResponse;
import com.justin.modelops.dashboard.dto.FastestModelEntry;
import com.justin.modelops.dashboard.dto.ModelDistributionResponse;
import com.justin.modelops.dashboard.service.DashboardService;
import com.justin.modelops.inference.dto.InferenceTaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Aggregated counters and averages")
    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary() {
        return ApiResponse.ok(dashboardService.summary());
    }

    @Operation(summary = "Ten most recent inference tasks")
    @GetMapping("/recent-tasks")
    public ApiResponse<List<InferenceTaskResponse>> recentTasks() {
        return ApiResponse.ok(dashboardService.recentTasks());
    }

    @Operation(summary = "Model counts by modality and format")
    @GetMapping("/model-distribution")
    public ApiResponse<ModelDistributionResponse> modelDistribution() {
        return ApiResponse.ok(dashboardService.modelDistribution());
    }

    @Operation(summary = "Models ranked by average inference throughput")
    @GetMapping("/fastest-models")
    public ApiResponse<List<FastestModelEntry>> fastestModels(
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(dashboardService.fastestModels(limit));
    }
}
