package com.cryptotax.helper.controller;

import com.cryptotax.helper.dto.TaxCalculationResultDto;
import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.TransactionRepository;
import com.cryptotax.helper.service.TaxCalculationService;
import com.cryptotax.helper.service.TransactionImportService;
import com.cryptotax.helper.service.ExchangeIntegrationService;
import com.cryptotax.helper.service.FifoTaxCalculationService;
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
    private final ExchangeIntegrationService exchangeIntegrationService;
    private final TransactionRepository transactionRepository; // ✅ ДОБАВЛЯЕМ
    private final FifoTaxCalculationService fifoTaxCalculationService; // ✅ ДОБАВЛЯЕМ

    @PostMapping("/import-demo")
    public ResponseEntity<?> importDemoTransactions() {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = 1L; // Временная заглушка

            // Создаем временного пользователя для демо
            User demoUser = new User();
            demoUser.setId(userId);

            List<Transaction> transactions = transactionImportService.importDemoTransactions(demoUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Демо транзакции успешно импортированы");
            response.put("importedCount", transactions.size());
            response.put("demoMode", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при импорте транзакций: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/import-demo-real")
    public ResponseEntity<?> importDemoWithRealPrices() {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = 1L; // Временная заглушка

            // Создаем временного пользователя для демо
            User demoUser = new User();
            demoUser.setId(userId);

            List<Transaction> transactions = exchangeIntegrationService.createDemoTransactions(demoUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Демо транзакции с реальными ценами успешно созданы");
            response.put("importedCount", transactions.size());
            response.put("demoMode", true);
            response.put("hasRealPrices", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при создании демо-транзакций: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/tax-calculation")
    public ResponseEntity<?> calculateTaxes(@RequestParam(defaultValue = "2024") int year) {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = 1L; // Временная заглушка

            TaxCalculationResultDto result = taxCalculationService.calculateTaxes(userId, year);

            Map<String, Object> response = new HashMap<>();
            response.put("taxYear", result.getTaxYear());
            response.put("totalIncome", result.getTotalIncome());
            response.put("totalExpenses", result.getTotalExpenses());
            response.put("taxableProfit", result.getTaxableProfit());
            response.put("taxAmount", result.getTaxAmount());
            response.put("transactionCount", result.getTransactionCount());
            response.put("demoMode", true);
            response.put("currency", result.getCurrency());
            response.put("country", result.getCountry());
            response.put("calculationMethod", result.getCalculationMethod());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при расчете налогов: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/tax/fifo-detailed")
    public ResponseEntity<?> calculateFifoTaxesDetailed(@RequestParam(defaultValue = "2024") int year) {
        try {
            Long userId = 1L; // Временная заглушка

            User user = new User();
            user.setId(userId);

            List<Transaction> transactions = transactionRepository.findByUserAndYearOrderByTimestampDesc(user, year);
            String country = "RUSSIA"; // Можно получить из профиля

            Map<String, Object> fifoResult = fifoTaxCalculationService.calculateFifoTaxes(transactions, country, year);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calculation", fifoResult);
            response.put("userId", userId);
            response.put("taxYear", year);
            response.put("country", country);
            response.put("demoMode", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка FIFO расчета: " + e.getMessage());
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
            response.put("demoMode", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении количества транзакций: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/clear-demo")
    public ResponseEntity<?> clearDemoTransactions() {
        try {
            // TODO: Реализовать очистку демо-транзакций
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Функция очистки демо-данных в разработке");
            response.put("demoMode", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при очистке демо-данных: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}