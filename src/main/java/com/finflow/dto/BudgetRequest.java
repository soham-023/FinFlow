package com.finflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO for creating a budget.
 */
public record BudgetRequest(
        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Monthly limit is required")
        @Positive(message = "Monthly limit must be positive")
        BigDecimal monthlyLimit,

        @NotBlank(message = "Month is required (format: YYYY-MM)")
        String month
) {}
