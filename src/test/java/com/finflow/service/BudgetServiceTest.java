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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetMapper budgetMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Budget budget;
    private BudgetRequest request;
    private BudgetResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Alex Smith")
                .email("alex@example.com")
                .build();

        request = new BudgetRequest("FOOD", new BigDecimal("500.00"), "2026-10");

        budget = Budget.builder()
                .id(1L)
                .category("FOOD")
                .monthlyLimit(new BigDecimal("500.00"))
                .month(YearMonth.of(2026, 10))
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        response = new BudgetResponse(
                1L,
                "FOOD",
                new BigDecimal("500.00"),
                "2026-10",
                new BigDecimal("200.00"),
                new BigDecimal("300.00")
        );
    }

    @Test
    @DisplayName("Should create budget and compute remaining amount")
    void shouldCreateBudget() {
        when(budgetMapper.toEntity(request, user)).thenReturn(budget);
        when(budgetRepository.save(budget)).thenReturn(budget);
        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(new BigDecimal("200.00"));
        when(budgetMapper.toResponse(budget, new BigDecimal("200.00"))).thenReturn(response);

        BudgetResponse result = budgetService.create(request, user);

        assertThat(result).isNotNull();
        assertThat(result.category()).isEqualTo("FOOD");
        assertThat(result.monthlyLimit()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.spent()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(result.remaining()).isEqualByComparingTo(new BigDecimal("300.00"));

        verify(budgetRepository).save(budget);
    }

    @Test
    @DisplayName("Should get all budgets for user with spent amounts")
    void shouldGetAllBudgets() {
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(new BigDecimal("200.00"));
        when(budgetMapper.toResponse(budget, new BigDecimal("200.00"))).thenReturn(response);

        List<BudgetResponse> results = budgetService.getAll(user);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).category()).isEqualTo("FOOD");
    }

    @Test
    @DisplayName("Should delete budget successfully when user is the owner")
    void shouldDeleteBudget() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        budgetService.delete(1L, user);

        verify(budgetRepository).delete(budget);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when deleting someone else's budget")
    void shouldThrowWhenDeletingOtherUsersBudget() {
        User otherUser = User.builder().id(99L).email("other@example.com").build();
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> budgetService.delete(1L, otherUser))
                .isInstanceOf(AccessDeniedException.class);

        verify(budgetRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when budget does not exist")
    void shouldThrowWhenBudgetNotFound() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.delete(99L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget not found");
    }
}
