package com.tracker.expense.tracker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tracker.expense.tracker.dto.BudgetResponse;
import com.tracker.expense.tracker.dto.TransactionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final BudgetService budgetService;
    private final TransactionService transactionService;

    @Value("${grok.api.key}")
    private String apiKey;

    @Value("${grok.api.url}")
    private String apiUrl;

    @Value("${grok.model}")
    private String model;

    private final RestClient restClient = RestClient.create();

    public String chat(String message, List<Map<String, String>> history) {

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt()));

        if (history != null) {
            int from = Math.max(0, history.size() - 10);
            for (Map<String, String> h : history.subList(from, history.size())) {
                String role = h.get("role");
                String content = h.get("content");
                if (content != null && ("user".equals(role) || "assistant".equals(role))) {
                    messages.add(Map.of("role", role, "content", content));
                }
            }
        }

        messages.add(Map.of("role", "user", "content", message));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.3);

        try {
            Map<?, ?> response = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> first = (Map<?, ?>) choices.get(0);
            Map<?, ?> msg = (Map<?, ?>) first.get("message");
            return String.valueOf(msg.get("content"));

        } catch (Exception e) {
            throw new RuntimeException("Assistant is unavailable: " + e.getMessage());
        }
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();

        sb.append("You are Aurum Assistant, a friendly and concise personal finance helper ")
          .append("inside the Aurum expense tracker app. Answer ONLY using the user's data below. ")
          .append("If the data does not contain the answer, say so. Do not invent numbers. ")
          .append("Use plain text only, no markdown, no asterisks. Keep answers short and clear. ")
          .append("Give practical, non-judgmental suggestions when asked.\n\n");

        sb.append("Today's date: ").append(LocalDate.now()).append("\n\n");

        sb.append("BUDGETS (category | month | limit | spent | remaining):\n");
        List<BudgetResponse> budgets = budgetService.getAllBudgets();
        if (budgets.isEmpty()) {
            sb.append("No budgets set.\n");
        }
        for (BudgetResponse b : budgets) {
            sb.append(b.getCategoryName()).append(" | ")
              .append(b.getMonthYear()).append(" | ")
              .append(b.getMonthlyLimit()).append(" | ")
              .append(b.getSpentSoFar()).append(" | ")
              .append(b.getRemaining()).append("\n");
        }

        sb.append("\nTRANSACTIONS, latest 50 (date | type | category | amount | description):\n");
        List<TransactionResponse> txs = transactionService.getAllTransactions().stream()
                .sorted(Comparator.comparing(TransactionResponse::getTransactionDate).reversed())
                .limit(50)
                .toList();
        if (txs.isEmpty()) {
            sb.append("No transactions yet.\n");
        }
        for (TransactionResponse t : txs) {
            sb.append(t.getTransactionDate()).append(" | ")
              .append(t.getType()).append(" | ")
              .append(t.getCategoryName()).append(" | ")
              .append(t.getAmount()).append(" | ")
              .append(t.getDescription() == null ? "" : t.getDescription()).append("\n");
        }

        return sb.toString();
    }
}
