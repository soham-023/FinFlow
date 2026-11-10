package com.finflow.config;

import com.finflow.repository.UserRepository;
import com.finflow.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthenticationFilterTest {

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(mock(JwtService.class), mock(UserRepository.class));

    private static MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        return request;
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/index.html", "/api/auth/login", "/api/auth/register",
            "/swagger-ui/index.html", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/swagger-config"})
    @DisplayName("Public paths should skip JWT processing")
    void shouldSkipPublicPaths(String path) {
        assertThat(filter.shouldNotFilter(request(path))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/transactions", "/api/budgets/1", "/api/analytics/monthly", "/api/authx"})
    @DisplayName("Protected paths should go through JWT processing")
    void shouldFilterProtectedPaths(String path) {
        assertThat(filter.shouldNotFilter(request(path))).isFalse();
    }
}
