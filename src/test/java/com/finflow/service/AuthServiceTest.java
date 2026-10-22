package com.finflow.service;

import com.finflow.dto.AuthRequest;
import com.finflow.dto.AuthResponse;
import com.finflow.dto.RegisterRequest;
import com.finflow.entity.User;
import com.finflow.exception.DuplicateResourceException;
import com.finflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Alex Smith")
                .email("alex@example.com")
                .passwordHash("encodedPassword")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void shouldRegisterUser() {
        RegisterRequest request = new RegisterRequest("Alex Smith", "alex@example.com", "password123");

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.email()).isEqualTo("alex@example.com");
        assertThat(response.name()).isEqualTo("Alex Smith");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("Alex Smith", "alex@example.com", "password123");

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        AuthRequest request = new AuthRequest("alex@example.com", "password123");

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.email()).isEqualTo("alex@example.com");

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("alex@example.com", "password123")
        );
    }

    @Test
    @DisplayName("Should throw exception when login credentials are invalid")
    void shouldThrowWhenInvalidCredentials() {
        AuthRequest request = new AuthRequest("alex@example.com", "wrongPassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
