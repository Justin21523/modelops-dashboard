package com.justin.modelops.common.exception;

import lombok.Getter;

/**
 * Base exception for expected, recoverable business failures. Carries an
 * {@link ErrorCode} that the global handler translates into an HTTP response.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
    }
}
