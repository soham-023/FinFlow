package com.finflow.dto;

import com.finflow.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction data.
 */
public record TransactionResponse(
        Long id,
        BigDecimal amount,
        TransactionType type,
        String category,
        String description,
        LocalDate transactionDate,
        LocalDateTime createdAt
) {}
