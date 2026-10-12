package com.tracker.expense.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRequest {

    @NotNull(message = "Category id is required")
    private Long categoryId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Type is required")
    private String type; // "INCOME" or "EXPENSE"

    private String description;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;
}