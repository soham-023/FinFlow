package com.finflow.dto;

import java.math.BigDecimal;

/**
 * Response DTO for monthly financial summary.
 */
public record MonthlySummaryResponse(
        String month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal savings
) {}
