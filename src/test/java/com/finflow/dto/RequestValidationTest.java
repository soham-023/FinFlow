package com.finflow.dto;

import com.finflow.entity.TransactionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean Validation rules on request DTOs, checked without starting Spring.
 */
class RequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private static Set<String> invalidFields(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private static TransactionRequest transaction(String amount) {
        return new TransactionRequest(new BigDecimal(amount), TransactionType.EXPENSE, "FOOD", "Lunch",
                LocalDate.of(2026, 10, 22));
    }

    @Test
    @DisplayName("A well-formed transaction should pass validation")
    void validTransaction() {
        assertThat(invalidFields(transaction("250.50"))).isEmpty();
    }

    @Test
    @DisplayName("Transaction amounts with more than 2 decimal places should be rejected")
    void transactionAmountScale() {
        assertThat(invalidFields(transaction("10.005"))).containsExactly("amount");
    }

    @Test
    @DisplayName("Transaction amounts that overflow NUMERIC(19,2) should be rejected")
    void transactionAmountPrecision() {
        assertThat(invalidFields(transaction("123456789012345678"))).containsExactly("amount");
        assertThat(invalidFields(transaction("12345678901234567.99"))).isEmpty();
    }

    @Test
    @DisplayName("Descriptions longer than the 255-character column should be rejected")
    void transactionDescriptionLength() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("1.00"), TransactionType.EXPENSE, "FOOD",
                "x".repeat(256), LocalDate.of(2026, 10, 22));
        assertThat(invalidFields(request)).containsExactly("description");
    }

    @Test
    @DisplayName("Categories longer than 50 characters should be rejected")
    void categoryLength() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("1.00"), TransactionType.EXPENSE,
                "C".repeat(51), null, LocalDate.of(2026, 10, 22));
        assertThat(invalidFields(request)).containsExactly("category");
        assertThat(invalidFields(new BudgetRequest("C".repeat(51), BigDecimal.TEN, "2026-10")))
                .containsExactly("category");
    }

    @Test
    @DisplayName("Passwords longer than BCrypt's 72-byte limit should be rejected")
    void registerPasswordLength() {
        assertThat(invalidFields(new RegisterRequest("Alex", "alex@example.com", "p".repeat(73))))
                .containsExactly("password");
        assertThat(invalidFields(new RegisterRequest("Alex", "alex@example.com", "p".repeat(72)))).isEmpty();
    }

    @Test
    @DisplayName("Names longer than 100 characters should be rejected")
    void registerNameLength() {
        assertThat(invalidFields(new RegisterRequest("n".repeat(101), "alex@example.com", "password123")))
                .containsExactly("name");
    }

    @Test
    @DisplayName("Budget limits with more than 2 decimal places should be rejected")
    void budgetLimitScale() {
        BudgetRequest request = new BudgetRequest("FOOD", new BigDecimal("99.999"), "2026-10");
        assertThat(invalidFields(request)).containsExactly("monthlyLimit");
    }
}
