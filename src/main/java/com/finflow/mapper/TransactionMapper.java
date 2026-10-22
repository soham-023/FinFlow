package com.finflow.mapper;

import com.finflow.dto.TransactionRequest;
import com.finflow.dto.TransactionResponse;
import com.finflow.entity.Transaction;
import com.finflow.entity.User;
import org.springframework.stereotype.Component;

/**
 * Maps between Transaction entity and DTOs.
 */
@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequest request, User user) {
        return Transaction.builder()
                .amount(request.amount())
                .type(request.type())
                .category(request.category().toUpperCase())
                .description(request.description())
                .transactionDate(request.transactionDate())
                .user(user)
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getCreatedAt()
        );
    }
}
