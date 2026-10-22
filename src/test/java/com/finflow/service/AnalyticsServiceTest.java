package com.finflow.service;

import com.finflow.dto.CategorySummaryResponse;
import com.finflow.dto.MonthlySummaryResponse;
import com.finflow.entity.TransactionType;
import com.finflow.entity.User;
import com.finflow.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User user;
    private YearMonth month;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Alex Smith")
                .email("alex@example.com")
                .build();

        month = YearMonth.of(2026, 10);
    }

    @Test
    @DisplayName("Should compute monthly summary with income, expense, and savings")
    void shouldGetMonthlySummary() {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.INCOME), eq(start), eq(end)
        )).thenReturn(new BigDecimal("5000.00"));

        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end)
        )).thenReturn(new BigDecimal("1850.50"));

        MonthlySummaryResponse summary = analyticsService.getMonthlySummary(user, month);

        assertThat(summary).isNotNull();
        assertThat(summary.month()).isEqualTo("2026-10");
        assertThat(summary.totalIncome()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(summary.totalExpense()).isEqualByComparingTo(new BigDecimal("1850.50"));
        assertThat(summary.savings()).isEqualByComparingTo(new BigDecimal("3149.50"));
    }

    @Test
    @DisplayName("Should return category-wise expense breakdown")
    void shouldGetCategorySummary() {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Object[]> queryResults = List.of(
                new Object[]{"FOOD", new BigDecimal("450.00")},
                new Object[]{"RENT", new BigDecimal("1000.00")},
                new Object[]{"ENTERTAINMENT", new BigDecimal("150.00")}
        );

        when(transactionRepository.sumAmountByCategoryAndUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end)
        )).thenReturn(queryResults);

        List<CategorySummaryResponse> summaries = analyticsService.getCategorySummary(user, month);

        assertThat(summaries).hasSize(3);
        assertThat(summaries.get(0).category()).isEqualTo("FOOD");
        assertThat(summaries.get(0).totalAmount()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(summaries.get(1).category()).isEqualTo("RENT");
        assertThat(summaries.get(1).totalAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }
}
