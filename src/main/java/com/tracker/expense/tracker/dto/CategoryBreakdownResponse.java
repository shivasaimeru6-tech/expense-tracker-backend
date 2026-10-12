package com.tracker.expense.tracker.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CategoryBreakdownResponse {

    private String categoryName;
    private String type;
    private BigDecimal totalAmount;
}