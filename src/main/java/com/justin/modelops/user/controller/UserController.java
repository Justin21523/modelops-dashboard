package com.justin.modelops.user.controller;

import com.justin.modelops.common.response.ApiResponse;
import com.justin.modelops.user.dto.ChangePasswordRequest;
import com.justin.modelops.user.dto.UpdateProfileRequest;
import com.justin.modelops.user.dto.UserResponse;
import com.justin.modelops.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get the currently authenticated user")
    @GetMapping("/me")
    public ApiResponse<UserResponse> currentUser(@AuthenticationPrincipal String username) {
        return ApiResponse.ok(userService.getByUsername(username));
    }

    @Operation(summary = "Update the current user's profile")
    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@AuthenticationPrincipal String username,
                                                   @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateProfile(username, request));
    }

    @Operation(summary = "Change the current user's password (revokes existing refresh tokens)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/me/change-password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal String username,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(username, request);
        return ApiResponse.ok();
    }
}
