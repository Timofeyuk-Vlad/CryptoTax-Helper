package com.cryptotax.helper.controller;

import com.cryptotax.helper.service.ExcelReportService;
import com.cryptotax.helper.service.PdfReportService;
import com.cryptotax.helper.service.ReportService;
import com.cryptotax.helper.util.SecurityUtils;
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
    private final ExcelReportService excelReportService;
    private final SecurityUtils securityUtils;

    @PostMapping("/tax")
    public ResponseEntity<?> generateTaxReport(@RequestParam(defaultValue = "2024") int year) {
        try {
            Long userId = securityUtils.getCurrentUserId(); // Временная заглушка

            Map<String, Object> report = reportService.generateTaxReport(userId, year);

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при генерации отчета: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/tax/pdf")
    public ResponseEntity<byte[]> generateTaxReportPdf(
            @RequestParam(defaultValue = "2024") int year,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        try {
            Long userId = securityUtils.getCurrentUserId();

            //log.info("🧾 Генерация PDF: user={}, year={}, from={}, to={}", userId, year, from, to);

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
            return ResponseEntity.badRequest()
                    .body(("Ошибка при генерации PDF: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/tax/excel")
    public ResponseEntity<byte[]> generateTaxReportExcel(@RequestParam(defaultValue = "2024") int year) {
        try {
            Long userId = securityUtils.getCurrentUserId(); // Временная заглушка

            byte[] excelBytes = excelReportService.generateTaxReportExcel(userId, year);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("filename",
                    "tax-report-" + year + ".xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при генерации Excel отчета: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(("Ошибка при генерации Excel: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/formats")
    public ResponseEntity<?> getAvailableFormats() {
        Map<String, Object> formats = new HashMap<>();
        formats.put("availableFormats", java.util.List.of(
                Map.of("format", "JSON", "description", "Машиночитаемый формат для API", "endpoint", "/api/reports/tax"),
                Map.of("format", "PDF", "description", "PDF документ для печати", "available", true, "endpoint", "/api/reports/tax/pdf"),
                Map.of("format", "EXCEL", "description", "Excel файл с детализацией операций", "available", true, "endpoint", "/api/reports/tax/excel")
        ));

        return ResponseEntity.ok(formats);
    }
}