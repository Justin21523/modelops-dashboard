package com.justin.modelops.model.controller;

import com.justin.modelops.common.response.ApiResponse;
import com.justin.modelops.common.response.PageResponse;
import com.justin.modelops.model.dto.CreateModelRequest;
import com.justin.modelops.model.dto.ModelFilter;
import com.justin.modelops.model.dto.ModelResponse;
import com.justin.modelops.model.dto.UpdateModelRequest;
import com.justin.modelops.model.enums.ModelFormatType;
import com.justin.modelops.model.enums.ModelModality;
import com.justin.modelops.model.enums.ModelStatus;
import com.justin.modelops.model.service.ModelService;
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

@Tag(name = "Models")
@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @Operation(summary = "Register a new model")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<ModelResponse> create(@Valid @RequestBody CreateModelRequest request) {
        return ApiResponse.ok(modelService.create(request));
    }

    @Operation(summary = "List models with optional filtering and pagination")
    @GetMapping
    public ApiResponse<PageResponse<ModelResponse>> list(
            @RequestParam(required = false) ModelModality modality,
            @RequestParam(required = false) ModelFormatType formatType,
            @RequestParam(required = false) ModelStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        ModelFilter filter = new ModelFilter(modality, formatType, status, keyword, tagId);
        return ApiResponse.ok(PageResponse.from(modelService.list(filter, pageable)));
    }

    @Operation(summary = "Get a model by id")
    @GetMapping("/{id}")
    public ApiResponse<ModelResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(modelService.get(id));
    }

    @Operation(summary = "Partially update a model")
    @PatchMapping("/{id}")
    public ApiResponse<ModelResponse> update(@PathVariable Long id,
                                             @Valid @RequestBody UpdateModelRequest request) {
        return ApiResponse.ok(modelService.update(id, request));
    }

    @Operation(summary = "Delete a model")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        modelService.delete(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "Attach a tag to a model")
    @PostMapping("/{id}/tags/{tagId}")
    public ApiResponse<ModelResponse> attachTag(@PathVariable Long id, @PathVariable Long tagId) {
        return ApiResponse.ok(modelService.attachTag(id, tagId));
    }

    @Operation(summary = "Detach a tag from a model")
    @DeleteMapping("/{id}/tags/{tagId}")
    public ApiResponse<ModelResponse> detachTag(@PathVariable Long id, @PathVariable Long tagId) {
        return ApiResponse.ok(modelService.detachTag(id, tagId));
    }
}
