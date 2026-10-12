package com.tracker.expense.tracker.service;

import com.tracker.expense.tracker.dto.BudgetRequest;
import com.tracker.expense.tracker.dto.BudgetResponse;
import com.tracker.expense.tracker.model.Budget;
import com.tracker.expense.tracker.model.Category;
import com.tracker.expense.tracker.model.Transaction;
import com.tracker.expense.tracker.model.User;
import com.tracker.expense.tracker.repository.BudgetRepository;
import com.tracker.expense.tracker.repository.CategoryRepository;
import com.tracker.expense.tracker.repository.TransactionRepository;
import com.tracker.expense.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter MONTH_YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public BudgetResponse createBudget(BudgetRequest request) {
        User user = getCurrentUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        budgetRepository.findByUserAndCategoryIdAndMonthYear(user, request.getCategoryId(), request.getMonthYear())
                .ifPresent(b -> {
                    throw new RuntimeException("Budget already exists for this category and month");
                });

        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .monthlyLimit(request.getMonthlyLimit())
                .monthYear(request.getMonthYear())
                .build();

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    public List<BudgetResponse> getAllBudgets() {
        User user = getCurrentUser();
        return budgetRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        User user = getCurrentUser();

        Budget budget = budgetRepository.findByUserAndId(user, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        budget.setCategory(category);
        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setMonthYear(request.getMonthYear());

        Budget updated = budgetRepository.save(budget);
        return toResponse(updated);
    }

    public void deleteBudget(Long id) {
        User user = getCurrentUser();

        Budget budget = budgetRepository.findByUserAndId(user, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Budget not found: " + id));

        budgetRepository.delete(budget);
    }

    private BigDecimal calculateSpent(User user, Long categoryId, String monthYear) {
        YearMonth ym = YearMonth.parse(monthYear, MONTH_YEAR_FORMAT);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
                .findByUserAndCategoryIdAndTransactionDateBetween(user, categoryId, start, end);

        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BudgetResponse toResponse(Budget budget) {
        User user = budget.getUser();
        BigDecimal spent = calculateSpent(user, budget.getCategory().getId(), budget.getMonthYear());
        BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);

        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryName(budget.getCategory().getName())
                .monthlyLimit(budget.getMonthlyLimit())
                .monthYear(budget.getMonthYear())
                .spentSoFar(spent)
                .remaining(remaining)
                .build();
    }
}