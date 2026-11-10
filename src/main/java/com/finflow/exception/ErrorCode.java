package com.finflow.exception;

/**
 * Stable, machine-readable error codes returned in {@link ErrorResponse#code()}.
 * Clients should branch on these rather than on the human-readable message, which may change.
 */
public enum ErrorCode {
    VALIDATION_FAILED,
    INVALID_PARAMETER,
    MALFORMED_REQUEST,
    INVALID_CREDENTIALS,
    UNAUTHENTICATED,
    ACCESS_DENIED,
    RESOURCE_NOT_FOUND,
    ENDPOINT_NOT_FOUND,
    METHOD_NOT_ALLOWED,
    UNSUPPORTED_MEDIA_TYPE,
    DUPLICATE_RESOURCE,
    INTERNAL_ERROR
}
