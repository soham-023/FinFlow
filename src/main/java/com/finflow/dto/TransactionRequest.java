package com.finflow.dto;

import com.finflow.entity.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a transaction.
 */
public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @Digits(integer = 17, fraction = 2, message = "Amount can have at most 17 integer digits and 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "Transaction type is required (INCOME or EXPENSE)")
        TransactionType type,

        @NotBlank(message = "Category is required")
        @Size(max = 50, message = "Category must be at most 50 characters")
        String category,

        @Size(max = 255, message = "Description must be at most 255 characters")
        String description,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate
) {}
