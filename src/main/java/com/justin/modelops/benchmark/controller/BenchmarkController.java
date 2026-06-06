package com.justin.modelops.benchmark.controller;

import com.justin.modelops.benchmark.dto.BenchmarkFilter;
import com.justin.modelops.benchmark.dto.BenchmarkResponse;
import com.justin.modelops.benchmark.dto.CreateBenchmarkRequest;
import com.justin.modelops.benchmark.dto.UpdateBenchmarkRequest;
import com.justin.modelops.benchmark.enums.BenchmarkType;
import com.justin.modelops.benchmark.service.BenchmarkService;
import com.justin.modelops.common.response.ApiResponse;
import com.justin.modelops.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Tag(name = "Benchmarks")
@RestController
@RequestMapping("/api/v1/benchmarks")
@RequiredArgsConstructor
public class BenchmarkController {

    private final BenchmarkService service;

    @Operation(summary = "Create a benchmark definition (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<BenchmarkResponse> create(@Valid @RequestBody CreateBenchmarkRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @Operation(summary = "List benchmark definitions with optional filtering")
    @GetMapping
    public ApiResponse<PageResponse<BenchmarkResponse>> list(
            @RequestParam(required = false) BenchmarkType type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        BenchmarkFilter filter = new BenchmarkFilter(type, keyword, tagId);
        return ApiResponse.ok(PageResponse.from(service.list(filter, pageable)));
    }

    @Operation(summary = "Get a benchmark definition by id")
    @GetMapping("/{id}")
    public ApiResponse<BenchmarkResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Partially update a benchmark definition (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ApiResponse<BenchmarkResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateBenchmarkRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "Delete a benchmark definition (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "Attach a tag to a benchmark (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/tags/{tagId}")
    public ApiResponse<BenchmarkResponse> attachTag(@PathVariable Long id, @PathVariable Long tagId) {
        return ApiResponse.ok(service.attachTag(id, tagId));
    }

    @Operation(summary = "Detach a tag from a benchmark (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/tags/{tagId}")
    public ApiResponse<BenchmarkResponse> detachTag(@PathVariable Long id, @PathVariable Long tagId) {
        return ApiResponse.ok(service.detachTag(id, tagId));
    }
}
