package com.justin.modelops.runtime.controller;

import com.justin.modelops.common.response.ApiResponse;
import com.justin.modelops.common.response.PageResponse;
import com.justin.modelops.runtime.dto.CreateRuntimeBackendRequest;
import com.justin.modelops.runtime.dto.RuntimeBackendResponse;
import com.justin.modelops.runtime.dto.UpdateRuntimeBackendRequest;
import com.justin.modelops.runtime.service.RuntimeBackendService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Runtime Backends")
@RestController
@RequestMapping("/api/v1/runtime-backends")
@RequiredArgsConstructor
public class RuntimeBackendController {

    private final RuntimeBackendService service;

    @Operation(summary = "Register a runtime backend")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<RuntimeBackendResponse> create(@Valid @RequestBody CreateRuntimeBackendRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @Operation(summary = "List runtime backends")
    @GetMapping
    public ApiResponse<PageResponse<RuntimeBackendResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.list(pageable)));
    }

    @Operation(summary = "Get a runtime backend by id")
    @GetMapping("/{id}")
    public ApiResponse<RuntimeBackendResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Partially update a runtime backend")
    @PatchMapping("/{id}")
    public ApiResponse<RuntimeBackendResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateRuntimeBackendRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "Delete a runtime backend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
