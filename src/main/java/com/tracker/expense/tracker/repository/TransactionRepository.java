package com.tracker.expense.tracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracker.expense.tracker.model.Transaction;
import com.tracker.expense.tracker.model.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser(User user);

    List<Transaction> findByUserAndId(User user, Long id);
    List<Transaction> findByUserAndTransactionDateBetween(User user, LocalDate start, LocalDate end);
    List<Transaction> findByUserAndCategoryIdAndTransactionDateBetween(
        User user, Long categoryId, java.time.LocalDate start, java.time.LocalDate end
);
}