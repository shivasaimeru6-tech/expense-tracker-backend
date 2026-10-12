package com.tracker.expense.tracker.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BudgetResponse {

    private Long id;
    private String categoryName;
    private BigDecimal monthlyLimit;
    private String monthYear;
    private BigDecimal spentSoFar;
    private BigDecimal remaining;
}
