package com.tracker.expense.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracker.expense.tracker.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}