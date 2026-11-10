package com.finflow.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response body.
 *
 * @param status    HTTP status code
 * @param code      machine-readable error code, stable across releases
 * @param message   human-readable description
 * @param timestamp when the error occurred
 * @param errors    per-field validation messages, or {@code null}
 */
public record ErrorResponse(
        int status,
        ErrorCode code,
        String message,
        LocalDateTime timestamp,
        Map<String, String> errors
) {
    public ErrorResponse(int status, ErrorCode code, String message) {
        this(status, code, message, LocalDateTime.now(), null);
    }
}
