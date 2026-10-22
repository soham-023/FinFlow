package com.finflow.dto;

import com.finflow.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a transaction.
 */
public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Transaction type is required (INCOME or EXPENSE)")
        TransactionType type,

        @NotBlank(message = "Category is required")
        String category,

        String description,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate
) {}
