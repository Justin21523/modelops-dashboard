package com.justin.modelops.user.controller;

import com.justin.modelops.common.response.ApiResponse;
import com.justin.modelops.user.dto.UserResponse;
import com.justin.modelops.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
