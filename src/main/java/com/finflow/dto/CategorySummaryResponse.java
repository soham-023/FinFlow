package com.finflow.dto;

import java.math.BigDecimal;

/**
 * Response DTO for category-wise spending breakdown.
 */
public record CategorySummaryResponse(
        String category,
        BigDecimal totalAmount
) {}
