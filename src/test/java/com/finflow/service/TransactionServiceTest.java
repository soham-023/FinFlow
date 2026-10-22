package com.finflow.service;

import com.finflow.dto.TransactionRequest;
import com.finflow.dto.TransactionResponse;
import com.finflow.entity.Transaction;
import com.finflow.entity.TransactionType;
import com.finflow.entity.User;
import com.finflow.exception.ResourceNotFoundException;
import com.finflow.mapper.TransactionMapper;
import com.finflow.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Transaction transaction;
    private TransactionRequest request;
    private TransactionResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        request = new TransactionRequest(
                new BigDecimal("250.00"),
                TransactionType.EXPENSE,
                "FOOD",
                "Lunch",
                LocalDate.of(2026, 10, 22)
        );

        transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("250.00"))
                .type(TransactionType.EXPENSE)
                .category("FOOD")
                .description("Lunch")
                .transactionDate(LocalDate.of(2026, 10, 22))
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        response = new TransactionResponse(
                1L,
                new BigDecimal("250.00"),
                TransactionType.EXPENSE,
                "FOOD",
                "Lunch",
                LocalDate.of(2026, 10, 22),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create a transaction successfully")
    void shouldCreateTransaction() {
        when(transactionMapper.toEntity(request, user)).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(response);

        TransactionResponse result = transactionService.create(request, user);

        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(result.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.category()).isEqualTo("FOOD");

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should get transaction by ID")
    void shouldGetTransactionById() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(response);

        TransactionResponse result = transactionService.getById(1L, user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw when transaction not found")
    void shouldThrowWhenTransactionNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getById(99L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    @DisplayName("Should get paginated transactions")
    void shouldGetPaginatedTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction));

        when(transactionRepository.findByUserId(user.getId(), pageable)).thenReturn(transactionPage);
        when(transactionMapper.toResponse(transaction)).thenReturn(response);

        Page<TransactionResponse> result = transactionService.getAll(user, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).category()).isEqualTo("FOOD");
    }

    @Test
    @DisplayName("Should delete transaction")
    void shouldDeleteTransaction() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        transactionService.delete(1L, user);

        verify(transactionRepository).delete(transaction);
    }

    @Test
    @DisplayName("Should update transaction")
    void shouldUpdateTransaction() {
        TransactionRequest updateRequest = new TransactionRequest(
                new BigDecimal("300.00"),
                TransactionType.EXPENSE,
                "TRAVEL",
                "Cab ride",
                LocalDate.of(2026, 10, 22)
        );

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(response);

        TransactionResponse result = transactionService.update(1L, updateRequest, user);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(any(Transaction.class));
    }
}
