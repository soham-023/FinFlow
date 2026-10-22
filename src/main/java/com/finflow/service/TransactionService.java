package com.finflow.service;

import com.finflow.dto.TransactionRequest;
import com.finflow.dto.TransactionResponse;
import com.finflow.entity.Transaction;
import com.finflow.entity.TransactionType;
import com.finflow.entity.User;
import com.finflow.exception.ResourceNotFoundException;
import com.finflow.mapper.TransactionMapper;
import com.finflow.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for managing financial transactions.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    /**
     * Create a new transaction for the authenticated user.
     */
    @Transactional
    public TransactionResponse create(TransactionRequest request, User user) {
        Transaction transaction = transactionMapper.toEntity(request, user);
        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    /**
     * Get a single transaction by ID, verifying ownership.
     */
    public TransactionResponse getById(Long id, User user) {
        Transaction transaction = findAndVerifyOwnership(id, user);
        return transactionMapper.toResponse(transaction);
    }

    /**
     * Get all transactions for the authenticated user with pagination.
     */
    public Page<TransactionResponse> getAll(User user, Pageable pageable) {
        return transactionRepository.findByUserId(user.getId(), pageable)
                .map(transactionMapper::toResponse);
    }

    /**
     * Get transactions filtered by type with pagination.
     */
    public Page<TransactionResponse> getByType(User user, TransactionType type, Pageable pageable) {
        return transactionRepository.findByUserIdAndType(user.getId(), type, pageable)
                .map(transactionMapper::toResponse);
    }

    /**
     * Get transactions filtered by category with pagination.
     */
    public Page<TransactionResponse> getByCategory(User user, String category, Pageable pageable) {
        return transactionRepository.findByUserIdAndCategory(user.getId(), category.toUpperCase(), pageable)
                .map(transactionMapper::toResponse);
    }

    /**
     * Update an existing transaction, verifying ownership.
     */
    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request, User user) {
        Transaction transaction = findAndVerifyOwnership(id, user);

        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setCategory(request.category().toUpperCase());
        transaction.setDescription(request.description());
        transaction.setTransactionDate(request.transactionDate());

        Transaction updated = transactionRepository.save(transaction);
        return transactionMapper.toResponse(updated);
    }

    /**
     * Delete a transaction, verifying ownership.
     */
    @Transactional
    public void delete(Long id, User user) {
        Transaction transaction = findAndVerifyOwnership(id, user);
        transactionRepository.delete(transaction);
    }

    /**
     * Find a transaction and verify it belongs to the given user.
     */
    private Transaction findAndVerifyOwnership(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this transaction");
        }

        return transaction;
    }
}
