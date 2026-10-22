package com.finflow.repository;

import com.finflow.entity.Transaction;
import com.finflow.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    Page<Transaction> findByUserIdAndType(Long userId, TransactionType type, Pageable pageable);

    Page<Transaction> findByUserIdAndCategory(Long userId, String category, Pageable pageable);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate start, LocalDate end);

    /**
     * Sum of amounts by transaction type for a user within a date range.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Category-wise sum of amounts for a user, type, and date range.
     * Returns rows of [category, totalAmount].
     */
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumAmountByCategoryAndUserAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
