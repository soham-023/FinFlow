package com.finflow.dto;

import java.math.BigDecimal;

/**
 * Response DTO for budget data, including spent and remaining amounts.
 */
public record BudgetResponse(
        Long id,
        String category,
        BigDecimal monthlyLimit,
        String month,
        BigDecimal spent,
        BigDecimal remaining
) {}
