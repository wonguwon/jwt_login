package com.kh.shopit.exception;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
            errorCode.getStatus().value(),
            errorCode.getMessage(),
            path,
            LocalDateTime.now()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(
            errorCode.getStatus().value(),
            message,
            path,
            LocalDateTime.now()
        );
    }

    public static ErrorResponse of(int status, String message, String path) {
        return new ErrorResponse(
            status,
            message,
            path,
            LocalDateTime.now()
        );
    }
} 