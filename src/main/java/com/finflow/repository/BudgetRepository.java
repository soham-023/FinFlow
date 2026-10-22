package com.finflow.repository;

import com.finflow.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByUserIdAndCategoryAndMonth(Long userId, String category, YearMonth month);

    List<Budget> findByUserIdAndMonth(Long userId, YearMonth month);
}
