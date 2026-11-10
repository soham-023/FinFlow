package com.finflow.controller;

import com.finflow.dto.CategorySummaryResponse;
import com.finflow.dto.MonthlySummaryResponse;
import com.finflow.repository.UserRepository;
import com.finflow.service.AnalyticsService;
import com.finflow.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("GET /api/analytics/monthly should return the summary for the requested month")
    void shouldReturnMonthlySummary() throws Exception {
        when(analyticsService.getMonthlySummary(any(), eq(YearMonth.of(2026, 10))))
                .thenReturn(new MonthlySummaryResponse("2026-10",
                        new BigDecimal("5000.00"), new BigDecimal("1850.50"), new BigDecimal("3149.50")));

        mockMvc.perform(get("/api/analytics/monthly").param("month", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-10"))
                .andExpect(jsonPath("$.savings").value(3149.50));
    }

    @Test
    @DisplayName("GET /api/analytics/monthly without month should default to the current month")
    void shouldDefaultToCurrentMonth() throws Exception {
        YearMonth now = YearMonth.now();
        when(analyticsService.getMonthlySummary(any(), any()))
                .thenReturn(new MonthlySummaryResponse(now.toString(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        mockMvc.perform(get("/api/analytics/monthly"))
                .andExpect(status().isOk());

        verify(analyticsService).getMonthlySummary(any(), eq(now));
    }

    @Test
    @DisplayName("GET /api/analytics/categories should return the category breakdown")
    void shouldReturnCategoryBreakdown() throws Exception {
        when(analyticsService.getCategorySummary(any(), eq(YearMonth.of(2026, 10))))
                .thenReturn(List.of(new CategorySummaryResponse("RENT", new BigDecimal("1000.00")),
                        new CategorySummaryResponse("FOOD", new BigDecimal("450.00"))));

        mockMvc.perform(get("/api/analytics/categories").param("month", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("RENT"));
    }

    @Test
    @DisplayName("Malformed month should return 400")
    void shouldRejectMalformedMonth() throws Exception {
        mockMvc.perform(get("/api/analytics/categories").param("month", "2026-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }
}
