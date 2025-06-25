package com.kh.shopit.exception;

public class UserAlreadyExistsException extends BaseException {
    public UserAlreadyExistsException() {
        super(ErrorCode.USER_ALREADY_EXISTS);
    }

    public UserAlreadyExistsException(String message) {
        super(ErrorCode.USER_ALREADY_EXISTS, message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(ErrorCode.USER_ALREADY_EXISTS, message, cause);
    }
} 