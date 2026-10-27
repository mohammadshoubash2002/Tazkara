package com.mohammad.booking_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String errorCode, String message, String path) {
        return new ErrorResponse(Instant.now(), status, errorCode, message, path, null);
    }

    public static ErrorResponse of(int status, String errorCode, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, errorCode, message, path, fieldErrors);
    }
}
