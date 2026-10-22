package com.finflow.controller;

import com.finflow.dto.CategorySummaryResponse;
import com.finflow.dto.MonthlySummaryResponse;
import com.finflow.entity.User;
import com.finflow.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

/**
 * REST endpoints for financial analytics and reporting.
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Financial analytics and reporting")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/monthly")
    @Operation(summary = "Monthly summary", description = "Get total income, expenses, and savings for a month")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String month) {

        YearMonth yearMonth = (month != null) ? YearMonth.parse(month) : YearMonth.now();
        MonthlySummaryResponse summary = analyticsService.getMonthlySummary(user, yearMonth);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/categories")
    @Operation(summary = "Category breakdown", description = "Get spending breakdown by category for a month")
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String month) {

        YearMonth yearMonth = (month != null) ? YearMonth.parse(month) : YearMonth.now();
        List<CategorySummaryResponse> summary = analyticsService.getCategorySummary(user, yearMonth);
        return ResponseEntity.ok(summary);
    }
}
