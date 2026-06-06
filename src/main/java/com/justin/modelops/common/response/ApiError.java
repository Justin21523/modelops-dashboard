package com.justin.modelops.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Structured error detail carried inside {@link ApiResponse} on failure.
 *
 * @param code         stable machine-readable error code
 * @param message      human-readable description
 * @param path         request path that produced the error
 * @param fieldErrors  per-field validation errors, when applicable
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String code, String message, String path, List<FieldViolation> fieldErrors) {

    public record FieldViolation(String field, String message) {
    }

    public static ApiError of(String code, String message, String path) {
        return new ApiError(code, message, path, null);
    }

    public static ApiError of(String code, String message, String path, List<FieldViolation> fieldErrors) {
        return new ApiError(code, message, path, fieldErrors);
    }
}
