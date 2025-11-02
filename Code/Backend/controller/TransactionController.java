package com.cryptotax.helper.controller;

import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.TransactionRepository;
import com.cryptotax.helper.service.FifoTaxCalculationService;
import com.cryptotax.helper.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final FifoTaxCalculationService fifoService;
    private final SecurityUtils securityUtils;

    /**
     * Расчёт налогов методом FIFO
     */
    @PostMapping("/tax/fifo-detailed")
    public ResponseEntity<?> calculateFifoTaxesDetailed(
            @RequestParam(defaultValue = "2024") int year,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        try {
            Long userId = securityUtils.getCurrentUserId();
            User user = new User();
            user.setId(userId);

            // 🔹 Извлекаем фильтры из JSON
            String asset = payload != null ? (String) payload.getOrDefault("asset", null) : null;
            String from = payload != null ? (String) payload.getOrDefault("from", null) : null;
            String to = payload != null ? (String) payload.getOrDefault("to", null) : null;

            log.info("📅 Расчёт за период: from={}, to={}, год={}, asset={}", from, to, year, asset);

            // Загружаем транзакции
            List<Transaction> transactions = transactionRepository.findByUserAndYearOrderByTimestampDesc(user, year);

            // 🔽 фильтрация по диапазону дат
            if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
                LocalDate fromDate = LocalDate.parse(from);
                LocalDate toDate = LocalDate.parse(to);
                transactions = transactions.stream()
                        .filter(tx -> {
                            LocalDate d = tx.getTimestamp().toLocalDate();
                            return !d.isBefore(fromDate) && !d.isAfter(toDate);
                        })
                        .collect(Collectors.toList());
            }

            // 🔽 фильтрация по валюте
            if (asset != null && !asset.isEmpty()) {
                String assetUpper = asset.toUpperCase();
                transactions = transactions.stream()
                        .filter(tx -> assetUpper.equalsIgnoreCase(tx.getBaseAsset()))
                        .collect(Collectors.toList());
            }

            String country = "RUSSIA";
            Map<String, Object> fifoResult =
                    fifoService.calculateFifoTaxes(transactions, country, year, asset);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("calculation", fifoResult);
            response.put("userId", userId);
            response.put("taxYear", year);
            response.put("country", country);
            response.put("filterAsset", asset);
            response.put("from", from);
            response.put("to", to);
            response.put("transactionCount", transactions.size());
            response.put("demoMode", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Ошибка FIFO расчёта", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка FIFO расчета: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
