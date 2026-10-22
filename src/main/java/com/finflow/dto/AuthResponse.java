package com.finflow.dto;

/**
 * Response DTO returned after successful authentication.
 */
public record AuthResponse(
        String token,
        String email,
        String name
) {}
