package com.finflow.service;

import com.finflow.dto.BudgetRequest;
import com.finflow.dto.BudgetResponse;
import com.finflow.entity.Budget;
import com.finflow.entity.TransactionType;
import com.finflow.entity.User;
import com.finflow.exception.DuplicateResourceException;
import com.finflow.exception.ResourceNotFoundException;
import com.finflow.mapper.BudgetMapper;
import com.finflow.repository.BudgetRepository;
import com.finflow.repository.TransactionRepository;
import com.finflow.util.Categories;
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
     *
     * @throws DuplicateResourceException if the user already has a budget for that category and month
     */
    @Transactional
    public BudgetResponse create(BudgetRequest request, User user) {
        Budget budget = budgetMapper.toEntity(request, user);

        budgetRepository.findByUserIdAndCategoryAndMonth(user.getId(), budget.getCategory(), budget.getMonth())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Budget already exists for category " + existing.getCategory()
                                    + " in " + existing.getMonth());
                });

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved, user);
    }

    /**
     * Get all budgets for the authenticated user with spent amounts.
     */
    public List<BudgetResponse> getAll(User user) {
        return toResponses(budgetRepository.findByUserId(user.getId()), user);
    }

    /**
     * Get budgets for a specific month with spent amounts.
     */
    public List<BudgetResponse> getByMonth(User user, YearMonth month) {
        return toResponses(budgetRepository.findByUserIdAndMonth(user.getId(), month), user);
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

    private List<BudgetResponse> toResponses(List<Budget> budgets, User user) {
        return budgets.stream()
                .map(budget -> toResponse(budget, user))
                .collect(Collectors.toList());
    }

    /**
     * Map a budget to its response, including how much has been spent against it so far.
     */
    private BudgetResponse toResponse(Budget budget, User user) {
        BigDecimal spent = calculateSpent(user, budget.getCategory(), budget.getMonth());
        return budgetMapper.toResponse(budget, spent);
    }

    /**
     * Calculate how much has been spent in a given category and month.
     * Only counts EXPENSE transactions.
     */
    private BigDecimal calculateSpent(User user, String category, YearMonth month) {
        return transactionRepository.sumAmountByUserAndTypeAndCategoryAndDateRange(
                user.getId(),
                TransactionType.EXPENSE,
                Categories.normalize(category),
                month.atDay(1),
                month.atEndOfMonth()
        );
    }
}
