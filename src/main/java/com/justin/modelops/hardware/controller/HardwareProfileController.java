package com.justin.modelops.hardware.controller;

import com.justin.modelops.common.response.ApiResponse;
import com.justin.modelops.common.response.PageResponse;
import com.justin.modelops.hardware.dto.CreateHardwareProfileRequest;
import com.justin.modelops.hardware.dto.HardwareProfileResponse;
import com.justin.modelops.hardware.dto.UpdateHardwareProfileRequest;
import com.justin.modelops.hardware.service.HardwareProfileService;
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

@Tag(name = "Hardware Profiles")
@RestController
@RequestMapping("/api/v1/hardware-profiles")
@RequiredArgsConstructor
public class HardwareProfileController {

    private final HardwareProfileService service;

    @Operation(summary = "Create a hardware profile")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<HardwareProfileResponse> create(@Valid @RequestBody CreateHardwareProfileRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @Operation(summary = "List hardware profiles")
    @GetMapping
    public ApiResponse<PageResponse<HardwareProfileResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(service.list(pageable)));
    }

    @Operation(summary = "Get a hardware profile by id")
    @GetMapping("/{id}")
    public ApiResponse<HardwareProfileResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "Partially update a hardware profile")
    @PatchMapping("/{id}")
    public ApiResponse<HardwareProfileResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateHardwareProfileRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "Delete a hardware profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }
}
