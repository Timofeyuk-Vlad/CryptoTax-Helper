package com.cryptotax.helper.controller;

import com.cryptotax.helper.dto.TaxCalculationResultDto;
import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.service.TaxCalculationService;
import com.cryptotax.helper.service.TransactionImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionImportService transactionImportService;
    private final TaxCalculationService taxCalculationService;

    @PostMapping("/import-demo")
    public ResponseEntity<?> importDemoTransactions() {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = 1L; // Временная заглушка

            // Создаем временного пользователя для демо
            com.cryptotax.helper.entity.User demoUser = new com.cryptotax.helper.entity.User();
            demoUser.setId(userId);

            List<Transaction> transactions = transactionImportService.importDemoTransactions(demoUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Демо транзакции успешно импортированы");
            response.put("importedCount", transactions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при импорте транзакций: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/tax-calculation")
    public ResponseEntity<?> calculateTaxes(@RequestParam(defaultValue = "2024") int year) {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = 1L; // Временная заглушка

            TaxCalculationResultDto result = taxCalculationService.calculateTaxes(userId, year);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при расчете налогов: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getTransactionCount() {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = 1L; // Временная заглушка

            long count = transactionImportService.getUserTransactionCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("transactionCount", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении количества транзакций: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}