package com.tracker.expense.tracker.controller;

import com.tracker.expense.tracker.dto.CategoryBreakdownResponse;
import com.tracker.expense.tracker.dto.SummaryResponse;
import com.tracker.expense.tracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @RequestParam int month,
            @RequestParam int year) {

        SummaryResponse response = analyticsService.getSummary(month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownResponse>> getCategoryBreakdown(
            @RequestParam int month,
            @RequestParam int year) {

        List<CategoryBreakdownResponse> response = analyticsService.getCategoryBreakdown(month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam int month,
            @RequestParam int year) {

        byte[] pdfBytes = analyticsService.generatePdfReport(month, year);

        String filename = "report-" + year + "-" + month + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}