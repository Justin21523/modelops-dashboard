package com.justin.modelops.common.exception;

/**
 * Thrown when a requested resource does not exist. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "%s not found: %s".formatted(resource, id));
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
