package com.finflow.mapper;

import com.finflow.dto.BudgetRequest;
import com.finflow.dto.BudgetResponse;
import com.finflow.dto.TransactionRequest;
import com.finflow.dto.TransactionResponse;
import com.finflow.entity.Budget;
import com.finflow.entity.Transaction;
import com.finflow.entity.TransactionType;
import com.finflow.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final TransactionMapper transactionMapper = new TransactionMapper();
    private final BudgetMapper budgetMapper = new BudgetMapper();

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Alex Smith")
                .email("alex@example.com")
                .passwordHash("hashed")
                .build();
    }

    @Test
    @DisplayName("TransactionMapper.toEntity should copy fields and upper-case the category")
    void transactionToEntity() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("250.00"), TransactionType.EXPENSE, "food", "Lunch", LocalDate.of(2026, 10, 22));

        Transaction entity = transactionMapper.toEntity(request, user);

        assertThat(entity.getAmount()).isEqualByComparingTo("250.00");
        assertThat(entity.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(entity.getCategory()).isEqualTo("FOOD");
        assertThat(entity.getDescription()).isEqualTo("Lunch");
        assertThat(entity.getTransactionDate()).isEqualTo(LocalDate.of(2026, 10, 22));
        assertThat(entity.getUser()).isSameAs(user);
    }

    @Test
    @DisplayName("TransactionMapper.toResponse should expose all response fields")
    void transactionToResponse() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 22, 12, 0);
        Transaction transaction = Transaction.builder()
                .id(7L)
                .amount(new BigDecimal("1000.00"))
                .type(TransactionType.INCOME)
                .category("SALARY")
                .description("October salary")
                .transactionDate(LocalDate.of(2026, 10, 1))
                .createdAt(createdAt)
                .user(user)
                .build();

        TransactionResponse response = transactionMapper.toResponse(transaction);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.amount()).isEqualByComparingTo("1000.00");
        assertThat(response.type()).isEqualTo(TransactionType.INCOME);
        assertThat(response.category()).isEqualTo("SALARY");
        assertThat(response.description()).isEqualTo("October salary");
        assertThat(response.transactionDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("BudgetMapper.toEntity should parse the month and upper-case the category")
    void budgetToEntity() {
        BudgetRequest request = new BudgetRequest("groceries", new BigDecimal("5000.00"), "2026-11");

        Budget budget = budgetMapper.toEntity(request, user);

        assertThat(budget.getCategory()).isEqualTo("GROCERIES");
        assertThat(budget.getMonthlyLimit()).isEqualByComparingTo("5000.00");
        assertThat(budget.getMonth()).isEqualTo(YearMonth.of(2026, 11));
        assertThat(budget.getUser()).isSameAs(user);
    }

    @Test
    @DisplayName("BudgetMapper.toResponse should compute remaining as limit minus spent")
    void budgetToResponseComputesRemaining() {
        Budget budget = Budget.builder()
                .id(3L)
                .category("FOOD")
                .monthlyLimit(new BigDecimal("500.00"))
                .month(YearMonth.of(2026, 10))
                .user(user)
                .build();

        BudgetResponse response = budgetMapper.toResponse(budget, new BigDecimal("620.50"));

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.month()).isEqualTo("2026-10");
        assertThat(response.spent()).isEqualByComparingTo("620.50");
        assertThat(response.remaining()).isEqualByComparingTo("-120.50");
    }
}
