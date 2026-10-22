package com.finflow.service;

import com.finflow.dto.BudgetRequest;
import com.finflow.dto.BudgetResponse;
import com.finflow.entity.Budget;
import com.finflow.entity.TransactionType;
import com.finflow.entity.User;
import com.finflow.exception.ResourceNotFoundException;
import com.finflow.mapper.BudgetMapper;
import com.finflow.repository.BudgetRepository;
import com.finflow.repository.TransactionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for managing monthly budgets.
 */
@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         BudgetMapper budgetMapper,
                         TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.budgetMapper = budgetMapper;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Create a new budget for a category and month.
     */
    @Transactional
    public BudgetResponse create(BudgetRequest request, User user) {
        Budget budget = budgetMapper.toEntity(request, user);
        Budget saved = budgetRepository.save(budget);

        BigDecimal spent = calculateSpent(user, saved.getCategory(), saved.getMonth());
        return budgetMapper.toResponse(saved, spent);
    }

    /**
     * Get all budgets for the authenticated user with spent amounts.
     */
    public List<BudgetResponse> getAll(User user) {
        return budgetRepository.findByUserId(user.getId()).stream()
                .map(budget -> {
                    BigDecimal spent = calculateSpent(user, budget.getCategory(), budget.getMonth());
                    return budgetMapper.toResponse(budget, spent);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get budgets for a specific month with spent amounts.
     */
    public List<BudgetResponse> getByMonth(User user, YearMonth month) {
        return budgetRepository.findByUserIdAndMonth(user.getId(), month).stream()
                .map(budget -> {
                    BigDecimal spent = calculateSpent(user, budget.getCategory(), budget.getMonth());
                    return budgetMapper.toResponse(budget, spent);
                })
                .collect(Collectors.toList());
    }

    /**
     * Delete a budget, verifying ownership.
     */
    @Transactional
    public void delete(Long id, User user) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this budget");
        }

        budgetRepository.delete(budget);
    }

    /**
     * Calculate how much has been spent in a given category and month.
     * Only counts EXPENSE transactions.
     */
    private BigDecimal calculateSpent(User user, String category, YearMonth month) {
        return transactionRepository.sumAmountByUserAndTypeAndDateRange(
                user.getId(),
                TransactionType.EXPENSE,
                month.atDay(1),
                month.atEndOfMonth()
        );
    }
}
