package com.finflow.controller;

import com.finflow.dto.TransactionRequest;
import com.finflow.dto.TransactionResponse;
import com.finflow.entity.TransactionType;
import com.finflow.entity.User;
import com.finflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for managing financial transactions.
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Income and expense management")
    
public class TransactionController {

    private final TransactionService transactionService;
    

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Add a transaction", description = "Create a new income or expense transaction")
    public ResponseEntity<TransactionResponse> create(
            @RequestBody @Valid TransactionRequest request,
            @AuthenticationPrincipal User user) {
        TransactionResponse response = transactionService.create(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List transactions", description = "Get paginated transactions with optional type/category filter")
    public ResponseEntity<Page<TransactionResponse>> getAll(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionResponse> result;
        if (type != null) {
            result = transactionService.getByType(user, type, pageable);
        } else if (category != null) {
            result = transactionService.getByCategory(user, category, pageable);
        } else {
            result = transactionService.getAll(user, pageable);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a transaction", description = "Get a single transaction by ID")
    public ResponseEntity<TransactionResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        TransactionResponse response = transactionService.getById(id, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction", description = "Update an existing transaction")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid TransactionRequest request,
            @AuthenticationPrincipal User user) {
        TransactionResponse response = transactionService.update(id, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction", description = "Delete a transaction by ID")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        transactionService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
