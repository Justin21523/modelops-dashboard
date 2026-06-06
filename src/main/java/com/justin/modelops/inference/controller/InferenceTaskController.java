package com.justin.modelops.inference.controller;

import com.justin.modelops.common.response.ApiResponse;
import com.justin.modelops.common.response.PageResponse;
import com.justin.modelops.inference.dto.CreateInferenceTaskRequest;
import com.justin.modelops.inference.dto.InferenceTaskResponse;
import com.justin.modelops.inference.enums.InferenceTaskStatus;
import com.justin.modelops.inference.service.InferenceTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inference Tasks")
@RestController
@RequestMapping("/api/v1/inference-tasks")
@RequiredArgsConstructor
public class InferenceTaskController {

    private final InferenceTaskService service;

    @Operation(summary = "Create a queued inference task")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<InferenceTaskResponse> create(@Valid @RequestBody CreateInferenceTaskRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @Operation(summary = "List inference tasks, optionally filtered by status")
    @GetMapping
    public ApiResponse<PageResponse<InferenceTaskResponse>> list(
            @RequestParam(required = false) InferenceTaskStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.list(status, pageable)));
    }

    @Operation(summary = "Get an inference task by id")
    @GetMapping("/{id}")
    public ApiResponse<InferenceTaskResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Execute a queued inference task asynchronously")
    @PostMapping("/{id}/run")
    public ApiResponse<InferenceTaskResponse> run(@PathVariable Long id) {
        return ApiResponse.ok(service.run(id));
    }

    @Operation(summary = "Cancel a queued or running inference task")
    @PostMapping("/{id}/cancel")
    public ApiResponse<InferenceTaskResponse> cancel(@PathVariable Long id) {
        return ApiResponse.ok(service.cancel(id));
    }

    @Operation(summary = "Attach a tag to an inference task")
    @PostMapping("/{id}/tags/{tagId}")
    public ApiResponse<InferenceTaskResponse> attachTag(@PathVariable Long id, @PathVariable Long tagId) {
        return ApiResponse.ok(service.attachTag(id, tagId));
    }

    @Operation(summary = "Detach a tag from an inference task")
    @DeleteMapping("/{id}/tags/{tagId}")
    public ApiResponse<InferenceTaskResponse> detachTag(@PathVariable Long id, @PathVariable Long tagId) {
        return ApiResponse.ok(service.detachTag(id, tagId));
    }
}
