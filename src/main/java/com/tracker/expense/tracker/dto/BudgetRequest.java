package com.tracker.expense.tracker.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BudgetRequest {

    @NotNull(message = "Category id is required")
    private Long categoryId;

    @NotNull(message = "Monthly limit is required")
    private BigDecimal monthlyLimit;

    @NotBlank(message = "Month/year is required")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Month/year must be in YYYY-MM format")
    private String monthYear;
}