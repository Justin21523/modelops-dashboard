package com.justin.modelops.tag.controller;

import com.justin.modelops.common.response.ApiResponse;
import com.justin.modelops.common.response.PageResponse;
import com.justin.modelops.tag.dto.CreateTagRequest;
import com.justin.modelops.tag.dto.TagResponse;
import com.justin.modelops.tag.dto.UpdateTagRequest;
import com.justin.modelops.tag.service.TagService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tags")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService service;

    @Operation(summary = "Create a tag (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<TagResponse> create(@Valid @RequestBody CreateTagRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @Operation(summary = "List tags")
    @GetMapping
    public ApiResponse<PageResponse<TagResponse>> list(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.list(pageable)));
    }

    @Operation(summary = "Get a tag by id")
    @GetMapping("/{id}")
    public ApiResponse<TagResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Partially update a tag (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ApiResponse<TagResponse> update(@PathVariable Long id,
                                           @Valid @RequestBody UpdateTagRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "Delete a tag (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
