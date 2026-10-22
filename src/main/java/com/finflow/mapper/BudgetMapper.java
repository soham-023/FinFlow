package com.finflow.mapper;

import com.finflow.dto.BudgetRequest;
import com.finflow.dto.BudgetResponse;
import com.finflow.entity.Budget;
import com.finflow.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Maps between Budget entity and DTOs.
 */
@Component
public class BudgetMapper {

    public Budget toEntity(BudgetRequest request, User user) {
        return Budget.builder()
                .category(request.category().toUpperCase())
                .monthlyLimit(request.monthlyLimit())
                .month(YearMonth.parse(request.month()))
                .user(user)
                .build();
    }

    public BudgetResponse toResponse(Budget budget, BigDecimal spent) {
        BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory(),
                budget.getMonthlyLimit(),
                budget.getMonth().toString(),
                spent,
                remaining
        );
    }
}
