package com.finflow.controller;

import com.finflow.dto.BudgetRequest;
import com.finflow.dto.BudgetResponse;
import com.finflow.entity.User;
import com.finflow.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Monthly budget management")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @Operation(summary = "Create a budget", description = "Set a monthly spending limit for a category")
    public ResponseEntity<BudgetResponse> create(
            @RequestBody @Valid BudgetRequest request,
            @AuthenticationPrincipal User user) {
        BudgetResponse response = budgetService.create(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "View budgets", description = "Get all budgets, optionally filtered by month (YYYY-MM)")
    public ResponseEntity<List<BudgetResponse>> getAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String month) {

        List<BudgetResponse> budgets;
        if (month != null) {
            budgets = budgetService.getByMonth(user, YearMonth.parse(month));
        } else {
            budgets = budgetService.getAll(user);
        }
        return ResponseEntity.ok(budgets);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget", description = "Delete a budget by ID")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        budgetService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
