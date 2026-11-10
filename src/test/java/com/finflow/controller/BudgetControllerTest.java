package com.finflow.controller;

import com.finflow.dto.BudgetRequest;
import com.finflow.dto.BudgetResponse;
import com.finflow.exception.DuplicateResourceException;
import com.finflow.repository.UserRepository;
import com.finflow.service.BudgetService;
import com.finflow.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetService budgetService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private final BudgetResponse food = new BudgetResponse(
            1L, "FOOD", new BigDecimal("500.00"), "2026-10", new BigDecimal("200.00"), new BigDecimal("300.00"));

    @Test
    @DisplayName("POST /api/budgets should create a budget and return 201")
    void shouldCreateBudget() throws Exception {
        when(budgetService.create(any(BudgetRequest.class), any())).thenReturn(food);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"food\",\"monthlyLimit\":500.00,\"month\":\"2026-10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("FOOD"))
                .andExpect(jsonPath("$.remaining").value(300.00));
    }

    @Test
    @DisplayName("POST /api/budgets should return 400 for an invalid month and a non-positive limit")
    void shouldRejectInvalidBudget() throws Exception {
        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"food\",\"monthlyLimit\":0,\"month\":\"2026-13\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.month").exists())
                .andExpect(jsonPath("$.errors.monthlyLimit").exists());

        verify(budgetService, never()).create(any(), any());
    }

    @Test
    @DisplayName("POST /api/budgets should return 409 for a duplicate budget")
    void shouldReturnConflictForDuplicate() throws Exception {
        when(budgetService.create(any(BudgetRequest.class), any()))
                .thenThrow(new DuplicateResourceException("Budget already exists for category FOOD in 2026-10"));

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"food\",\"monthlyLimit\":500.00,\"month\":\"2026-10\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @DisplayName("GET /api/budgets without month should list all budgets")
    void shouldListAllBudgets() throws Exception {
        when(budgetService.getAll(any())).thenReturn(List.of(food));

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("FOOD"));

        verify(budgetService, never()).getByMonth(any(), any());
    }

    @Test
    @DisplayName("GET /api/budgets?month= should filter by month")
    void shouldListBudgetsForMonth() throws Exception {
        when(budgetService.getByMonth(any(), eq(YearMonth.of(2026, 10)))).thenReturn(List.of(food));

        mockMvc.perform(get("/api/budgets").param("month", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2026-10"));
    }

    @Test
    @DisplayName("GET /api/budgets with a malformed month should return 400")
    void shouldRejectMalformedMonth() throws Exception {
        mockMvc.perform(get("/api/budgets").param("month", "October"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    @DisplayName("DELETE /api/budgets/{id} should return 204")
    void shouldDeleteBudget() throws Exception {
        mockMvc.perform(delete("/api/budgets/1"))
                .andExpect(status().isNoContent());

        verify(budgetService).delete(eq(1L), any());
    }
}
