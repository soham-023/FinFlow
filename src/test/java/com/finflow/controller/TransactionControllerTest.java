package com.finflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.dto.TransactionRequest;
import com.finflow.dto.TransactionResponse;
import com.finflow.entity.TransactionType;
import com.finflow.entity.User;
import com.finflow.repository.UserRepository;
import com.finflow.service.JwtService;
import com.finflow.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;
    

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Alex Smith")
                .email("alex@example.com")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/transactions should create transaction and return 201")
    void shouldCreateTransaction() throws Exception {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("250.00"),
           
                TransactionType.EXPENSE,
                "FOOD",
                "Lunch with colleagues",
                LocalDate.of(2026, 10, 22)
        );

        TransactionResponse response = new TransactionResponse(
                1L,
                new BigDecimal("250.00"),
                TransactionType.EXPENSE,
                "FOOD",
                "Lunch with colleagues",
                LocalDate.of(2026, 10, 22),
                LocalDateTime.now()
        );

        when(transactionService.create(any(TransactionRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("POST /api/transactions should return 400 when amount is negative")
    void shouldRejectNegativeAmount() throws Exception {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("-50.00"),
                TransactionType.EXPENSE,
                "FOOD",
                "Invalid amount",
                LocalDate.of(2026, 10, 22)
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    @DisplayName("GET /api/transactions/{id} should return 200 with transaction data")
    void shouldGetTransactionById() throws Exception {
        TransactionResponse response = new TransactionResponse(
                1L,
                new BigDecimal("150.00"),
                TransactionType.EXPENSE,
                "TRAVEL",
                "Train ticket",
                LocalDate.of(2026, 10, 22),
                LocalDateTime.now()
        );

        when(transactionService.getById(eq(1L), any())).thenReturn(response);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.category").value("TRAVEL"));
    }

    @Test
    @DisplayName("DELETE /api/transactions/{id} should return 204 No Content")
    void shouldDeleteTransaction() throws Exception {
        doNothing().when(transactionService).delete(eq(1L), any());

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isNoContent());
    }
}
