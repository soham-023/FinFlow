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

import java.util.Set;

/**
 * REST endpoints for managing financial transactions.
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Income and expense management")
    
public class TransactionController {

    /** Largest page a client may request, to keep queries and payloads bounded. */
    static final int MAX_PAGE_SIZE = 100;

    /** Fields clients may sort by; anything else would surface as a 500 from Spring Data. */
    static final Set<String> SORTABLE_FIELDS = Set.of("transactionDate", "amount", "category", "type", "createdAt");

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

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

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

    /**
     * Validate paging and sorting parameters and turn them into a {@link Pageable}.
     *
     * @throws IllegalArgumentException (mapped to 400) when a parameter is out of range
     */
    static Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        if (page < 0) {
            throw new IllegalArgumentException("Parameter 'page' must be 0 or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Parameter 'size' must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Parameter 'sortBy' must be one of " + SORTABLE_FIELDS.stream().sorted().toList());
        }
        Sort.Direction direction;
        if (sortDir.equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        } else if (sortDir.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        } else {
            throw new IllegalArgumentException("Parameter 'sortDir' must be 'asc' or 'desc'");
        }
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
