package com.cryptotax.helper.controller;

import com.cryptotax.helper.service.PdfReportService;
import com.cryptotax.helper.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final PdfReportService pdfReportService;

    @PostMapping("/tax")
    public ResponseEntity<?> generateTaxReport(@RequestParam(defaultValue = "2024") int year) {
        try {
            Long userId = 1L; // Временная заглушка

            Map<String, Object> report = reportService.generateTaxReport(userId, year);

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при генерации отчета: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/tax/pdf")
    public ResponseEntity<byte[]> generateTaxReportPdf(@RequestParam(defaultValue = "2024") int year) {
        try {
            Long userId = 1L; // Временная заглушка

            byte[] pdfBytes = pdfReportService.generateTaxReportPdf(userId, year);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename",
                    "tax-report-" + year + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            // Для PDF возвращаем ошибку в виде простого текста
            return ResponseEntity.badRequest()
                    .body(("Ошибка при генерации PDF: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/tax/pdf-html")
    public ResponseEntity<byte[]> generateTaxReportPdfHtml(@RequestParam(defaultValue = "2024") int year) {
        try {
            Long userId = 1L; // Временная заглушка

            byte[] pdfBytes = pdfReportService.generateHtmlTaxReportPdf(userId, year);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename",
                    "tax-report-html-" + year + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(("Ошибка при генерации HTML PDF: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/formats")
    public ResponseEntity<?> getAvailableFormats() {
        Map<String, Object> formats = new HashMap<>();
        formats.put("availableFormats", java.util.List.of(
                Map.of("format", "JSON", "description", "Машиночитаемый формат для API", "endpoint", "/api/reports/tax"),
                Map.of("format", "PDF", "description", "PDF документ для печати", "available", true, "endpoint", "/api/reports/tax/pdf"),
                Map.of("format", "PDF_HTML", "description", "PDF с HTML разметкой", "available", true, "endpoint", "/api/reports/tax/pdf-html"),
                Map.of("format", "EXCEL", "description", "Excel файл с детализацией", "available", false)
        ));

        return ResponseEntity.ok(formats);
    }
}