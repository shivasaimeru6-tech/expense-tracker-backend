package com.tracker.expense.tracker.service;

import com.tracker.expense.tracker.dto.CategoryBreakdownResponse;
import com.tracker.expense.tracker.dto.SummaryResponse;
import com.tracker.expense.tracker.model.Transaction;
import com.tracker.expense.tracker.model.TransactionType;
import com.tracker.expense.tracker.model.User;
import com.tracker.expense.tracker.repository.TransactionRepository;
import com.tracker.expense.tracker.repository.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public SummaryResponse getSummary(int month, int year) {
        User user = getCurrentUser();
        List<Transaction> transactions = getMonthTransactions(user, month, year);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        return SummaryResponse.builder()
                .monthYear(String.format("%02d-%d", month, year))
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .build();
    }

    public List<CategoryBreakdownResponse> getCategoryBreakdown(int month, int year) {
        User user = getCurrentUser();
        List<Transaction> transactions = getMonthTransactions(user, month, year);

        Map<String, List<Transaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getName()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String categoryName = entry.getKey();
                    List<Transaction> txs = entry.getValue();
                    BigDecimal total = txs.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    String type = txs.get(0).getType().name();

                    return CategoryBreakdownResponse.builder()
                            .categoryName(categoryName)
                            .type(type)
                            .totalAmount(total)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public byte[] generatePdfReport(int month, int year) {

        SummaryResponse summary = getSummary(month, year);
        List<CategoryBreakdownResponse> breakdown = getCategoryBreakdown(month, year);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Expense & Budget Report - " + summary.getMonthYear(), titleFont);
            title.setSpacingAfter(20);
            document.add(title);

            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            document.add(new Paragraph("Summary", sectionFont));
            document.add(new Paragraph("Total Income: " + summary.getTotalIncome()));
            document.add(new Paragraph("Total Expense: " + summary.getTotalExpense()));
            document.add(new Paragraph("Net Savings: " + summary.getNetSavings()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Category Breakdown", sectionFont));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Category");
            table.addCell("Type");
            table.addCell("Total Amount");

            for (CategoryBreakdownResponse item : breakdown) {
                table.addCell(item.getCategoryName());
                table.addCell(item.getType());
                table.addCell(item.getTotalAmount().toString());
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private List<Transaction> getMonthTransactions(User user, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return transactionRepository.findByUserAndTransactionDateBetween(user, start, end);
    }
}