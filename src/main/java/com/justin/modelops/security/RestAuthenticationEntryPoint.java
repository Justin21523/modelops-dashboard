package com.justin.modelops.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.common.response.ApiError;
import com.justin.modelops.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns the uniform {@link ApiResponse} envelope (HTTP 401) when an unauthenticated
 * request reaches a protected endpoint.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ApiError error = ApiError.of(ErrorCode.AUTHENTICATION_FAILED.name(),
                "Authentication is required to access this resource", request.getRequestURI());
        response.setStatus(ErrorCode.AUTHENTICATION_FAILED.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(error));
    }
}
