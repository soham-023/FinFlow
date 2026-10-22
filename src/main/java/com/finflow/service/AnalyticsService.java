package com.finflow.service;

import com.finflow.dto.CategorySummaryResponse;
import com.finflow.dto.MonthlySummaryResponse;
import com.finflow.entity.TransactionType;
import com.finflow.entity.User;
import com.finflow.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for financial analytics and reporting.
 */
@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public AnalyticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Get monthly summary: total income, total expense, and savings.
     */
    public MonthlySummaryResponse getMonthlySummary(User user, YearMonth month) {
        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                user.getId(),
                TransactionType.INCOME,
                month.atDay(1),
                month.atEndOfMonth()
        );

        BigDecimal totalExpense = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                user.getId(),
                TransactionType.EXPENSE,
                month.atDay(1),
                month.atEndOfMonth()
        );

        BigDecimal savings = totalIncome.subtract(totalExpense);

        return new MonthlySummaryResponse(
                month.toString(),
                totalIncome,
                totalExpense,
                savings
        );
    }

    /**
     * Get category-wise expense breakdown for a given month.
     */
    public List<CategorySummaryResponse> getCategorySummary(User user, YearMonth month) {
        List<Object[]> results = transactionRepository.sumAmountByCategoryAndUserAndTypeAndDateRange(
                user.getId(),
                TransactionType.EXPENSE,
                month.atDay(1),
                month.atEndOfMonth()
        );

        return results.stream()
                .map(row -> new CategorySummaryResponse(
                        (String) row[0],
                        (BigDecimal) row[1]
                ))
                .collect(Collectors.toList());
    }
}
