package com.finflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.dto.AuthRequest;
import com.finflow.dto.AuthResponse;
import com.finflow.dto.RegisterRequest;
import com.finflow.exception.DuplicateResourceException;
import com.finflow.service.AuthService;
import com.finflow.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private com.finflow.repository.UserRepository userRepository;

    @Test
    @DisplayName("POST /api/auth/register should return 201 on success")
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest("Alex Smith", "alex@example.com", "password123");
        AuthResponse response = new AuthResponse("jwt.token.here", "alex@example.com", "Alex Smith");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.email").value("alex@example.com"))
                .andExpect(jsonPath("$.name").value("Alex Smith"));
    }

    @Test
    @DisplayName("POST /api/auth/register with invalid email should return 400")
    void shouldRejectInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("Alex Smith", "not-an-email", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register with duplicate email should return 409")
    void shouldReturnConflictOnDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("Alex Smith", "alex@example.com", "password123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("Email already registered: alex@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered: alex@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 200 with JWT token")
    void shouldLoginSuccessfully() throws Exception {
        AuthRequest request = new AuthRequest("alex@example.com", "password123");
        AuthResponse response = new AuthResponse("jwt.token.here", "alex@example.com", "Alex Smith");

        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.email").value("alex@example.com"));
    }
}
