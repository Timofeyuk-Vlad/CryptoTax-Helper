package com.cryptotax.helper.controller;

import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.service.ExchangeIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExchangeImportController {

    private final ExchangeIntegrationService exchangeIntegrationService;

    @PostMapping("/exchange/{connectionId}")
    public ResponseEntity<?> importFromExchange(@PathVariable Long connectionId) {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = 1L; // Временная заглушка

            // Создаем временного пользователя
            com.cryptotax.helper.entity.User user = new com.cryptotax.helper.entity.User();
            user.setId(userId);

            List<Transaction> transactions = exchangeIntegrationService.importFromExchange(connectionId, user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Транзакции успешно импортированы");
            response.put("importedCount", transactions.size());
            response.put("connectionId", connectionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при импорте транзакций: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/validate-keys")
    public ResponseEntity<?> validateApiKeys(
            @RequestParam String exchange,
            @RequestParam String apiKey,
            @RequestParam String apiSecret) {
        try {
            com.cryptotax.helper.entity.Exchange exchangeEnum =
                    com.cryptotax.helper.entity.Exchange.valueOf(exchange.toUpperCase());

            boolean isValid = exchangeIntegrationService.validateExchangeConnection(exchangeEnum, apiKey, apiSecret);

            Map<String, Object> response = new HashMap<>();
            response.put("isValid", isValid);
            response.put("message", isValid ? "API ключи валидны" : "Неверные API ключи");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка валидации API ключей: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}