package com.tracker.expense.tracker.service;

import com.tracker.expense.tracker.dto.TransactionRequest;
import com.tracker.expense.tracker.dto.TransactionResponse;
import com.tracker.expense.tracker.model.Category;
import com.tracker.expense.tracker.model.Transaction;
import com.tracker.expense.tracker.model.TransactionType;
import com.tracker.expense.tracker.model.User;
import com.tracker.expense.tracker.repository.CategoryRepository;
import com.tracker.expense.tracker.repository.TransactionRepository;
import com.tracker.expense.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = getCurrentUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .type(TransactionType.valueOf(request.getType().toUpperCase()))
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public List<TransactionResponse> getAllTransactions() {
        User user = getCurrentUser();
        return transactionRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        User user = getCurrentUser();

        Transaction transaction = transactionRepository.findByUserAndId(user, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.valueOf(request.getType().toUpperCase()));
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());

        Transaction updated = transactionRepository.save(transaction);
        return toResponse(updated);
    }

    public void deleteTransaction(Long id) {
        User user = getCurrentUser();

        Transaction transaction = transactionRepository.findByUserAndId(user, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));

        transactionRepository.delete(transaction);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .categoryName(transaction.getCategory().getName())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }
}