package com.tracker.expense.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String categoryName;
    private BigDecimal amount;
    private String type;
    private String description;
    private LocalDate transactionDate;
}