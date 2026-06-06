package com.justin.modelops.evaluation.controller;

import com.justin.modelops.common.response.ApiResponse;
import com.justin.modelops.common.response.PageResponse;
import com.justin.modelops.evaluation.dto.CreateEvaluationRecordRequest;
import com.justin.modelops.evaluation.dto.EvaluationAggregateResponse;
import com.justin.modelops.evaluation.dto.EvaluationFilter;
import com.justin.modelops.evaluation.dto.EvaluationRecordResponse;
import com.justin.modelops.evaluation.dto.UpdateEvaluationRecordRequest;
import com.justin.modelops.evaluation.enums.EvaluationStatus;
import com.justin.modelops.evaluation.service.EvaluationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Evaluations")
@RestController
@RequestMapping("/api/v1/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationRecordService service;

    @Operation(summary = "Create an evaluation record")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<EvaluationRecordResponse> create(@Valid @RequestBody CreateEvaluationRecordRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @Operation(summary = "List evaluation records with optional filtering")
    @GetMapping
    public ApiResponse<PageResponse<EvaluationRecordResponse>> list(
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) Long benchmarkId,
            @RequestParam(required = false) Long hardwareProfileId,
            @RequestParam(required = false) Long runtimeBackendId,
            @RequestParam(required = false) EvaluationStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        EvaluationFilter filter = new EvaluationFilter(modelId, benchmarkId, hardwareProfileId,
                runtimeBackendId, status);
        return ApiResponse.ok(PageResponse.from(service.list(filter, pageable)));
    }

    @Operation(summary = "Aggregate evaluation metrics per model, optionally for one benchmark")
    @GetMapping("/aggregate")
    public ApiResponse<List<EvaluationAggregateResponse>> aggregate(
            @RequestParam(required = false) Long benchmarkId) {
        return ApiResponse.ok(service.aggregateByModel(benchmarkId));
    }

    @Operation(summary = "Get an evaluation record by id")
    @GetMapping("/{id}")
    public ApiResponse<EvaluationRecordResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Partially update an evaluation record")
    @PatchMapping("/{id}")
    public ApiResponse<EvaluationRecordResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateEvaluationRecordRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "Delete an evaluation record")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
