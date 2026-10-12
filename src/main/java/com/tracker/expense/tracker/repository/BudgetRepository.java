package com.tracker.expense.tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracker.expense.tracker.model.Budget;
import com.tracker.expense.tracker.model.User;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUser(User user);

    List<Budget> findByUserAndId(User user, Long id);

    Optional<Budget> findByUserAndCategoryIdAndMonthYear(User user, Long categoryId, String monthYear);
}