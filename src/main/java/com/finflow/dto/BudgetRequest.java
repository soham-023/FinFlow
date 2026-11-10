package com.finflow.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;


import java.math.BigDecimal;

/**
 * Request DTO for creating a budget.
 */
public record BudgetRequest(
        @NotBlank(message = "Category is required")
        @Size(max = 50, message = "Category must be at most 50 characters")
        String category,

        @NotNull(message = "Monthly limit is required")
        @Positive(message = "Monthly limit must be positive")
        @Digits(integer = 17, fraction = 2, message = "Monthly limit can have at most 17 integer digits and 2 decimal places")
        BigDecimal monthlyLimit,

        @NotBlank(message = "Month is required (format: YYYY-MM)")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Month must be in format YYYY-MM")
        String month
) {}
