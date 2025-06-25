package com.kh.shopit.exception;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }

    public InvalidCredentialsException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(ErrorCode.INVALID_CREDENTIALS, message, cause);
    }
} 