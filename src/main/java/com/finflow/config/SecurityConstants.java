package com.finflow.config;

/**
 * Security-related constants shared by {@link SecurityConfig} and {@link JwtAuthenticationFilter},
 * so the list of public endpoints is defined in exactly one place.
 */
public final class SecurityConstants {

    /** HTTP header carrying the JWT. */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** Scheme prefix that precedes the token in the Authorization header. */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * Ant-style path patterns that are reachable without authentication:
     * the static dashboard, auth endpoints, and the Swagger / OpenAPI docs.
     */
    public static final String[] PUBLIC_PATHS = {
            "/",
            "/index.html",
            "/favicon.ico",
            "/css/**",
            "/js/**",
            "/images/**",
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    private SecurityConstants() {
    }
}
