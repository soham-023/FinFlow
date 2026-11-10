package com.finflow.service;

import com.finflow.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // Base64 of a 256-bit test key; never used outside tests
    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3Rpbmctb25seQ==";
    private static final String OTHER_SECRET = "YW5vdGhlci10ZXN0LXNlY3JldC1rZXktMzItYnl0ZXMtbG9uZyE=";

    private JwtService jwtService;
    private User alex;

    private static JwtService newService(String secret, long expirationMillis) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", secret);
        ReflectionTestUtils.setField(service, "jwtExpiration", expirationMillis);
        return service;
    }

    @BeforeEach
    void setUp() {
        jwtService = newService(SECRET, 60_000);
        alex = User.builder().id(1L).name("Alex").email("alex@example.com").passwordHash("x").build();
    }

    @Test
    @DisplayName("Generated token should carry the user's email as subject")
    void shouldEmbedUsername() {
        String token = jwtService.generateToken(alex);

        assertThat(jwtService.extractUsername(token)).isEqualTo("alex@example.com");
    }

    @Test
    @DisplayName("Token should be valid for the user it was issued to")
    void shouldValidateForSameUser() {
        String token = jwtService.generateToken(alex);

        assertThat(jwtService.isTokenValid(token, alex)).isTrue();
    }

    @Test
    @DisplayName("Token should be invalid for a different user")
    void shouldRejectDifferentUser() {
        String token = jwtService.generateToken(alex);
        User other = User.builder().id(2L).email("other@example.com").passwordHash("x").build();

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    @DisplayName("Extra claims should be readable from the token")
    void shouldIncludeExtraClaims() {
        String token = jwtService.generateToken(Map.of("role", "USER"), alex);

        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        assertThat(role).isEqualTo("USER");
    }

    @Test
    @DisplayName("Expired token should be rejected")
    void shouldRejectExpiredToken() {
        String token = newService(SECRET, -1_000).generateToken(alex);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, alex))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Token signed with another key should be rejected")
    void shouldRejectForeignSignature() {
        String token = newService(OTHER_SECRET, 60_000).generateToken(alex);

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(SignatureException.class);
    }
}
