package com.tracker.expense.tracker.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    @NotBlank(message = "Message is required")
    private String message;

    private List<Map<String, String>> history;
}
