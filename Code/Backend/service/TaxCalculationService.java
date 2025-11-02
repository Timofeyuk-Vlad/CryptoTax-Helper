package com.cryptotax.helper.service;

import com.cryptotax.helper.dto.TaxCalculationResultDto;
import com.cryptotax.helper.entity.TaxProfile;
import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.TaxProfileRepository;
import com.cryptotax.helper.repository.TransactionRepository;
import com.cryptotax.helper.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxCalculationService {

    private final TransactionRepository transactionRepository;
    private final TaxProfileRepository taxProfileRepository;
    private final NotificationService notificationService;
    private final FifoTaxCalculationService fifoService;
    private final SecurityUtils securityUtils;

    public TaxCalculationResultDto calculateTaxes(Long userId, int taxYear) {
        User user = new User();
        user.setId(userId);

        log.info("📊 Расчет налогов для пользователя {} за {} год", userId, taxYear);

        // Страна
        String country = getTaxCountry(userId);

        // Получаем транзакции
        List<Transaction> transactions = transactionRepository.findByUserAndYearOrderByTimestampDesc(user, taxYear);

        // Если транзакций нет — не упадём
        if (transactions.isEmpty()) {
            log.warn("⚠️ У пользователя {} нет транзакций за {} год", userId, taxYear);
        }

        // FIFO расчет (без фильтра валюты)
        Map<String, Object> fifoResult = fifoService.calculateFifoTaxes(transactions, country, taxYear, null);

// Проверяем успешность
        if (!(Boolean) fifoResult.getOrDefault("success", true)) {
            log.warn("⚠️ Расчет FIFO не выполнен: {}", fifoResult.get("message"));
            throw new IllegalStateException("Расчет FIFO не выполнен: " + fifoResult.get("message"));
        }

// Извлекаем подрезультат
        Object calcObj = fifoResult.get("fifoCalculation");
        if (!(calcObj instanceof Map)) {
            log.error("❌ Неверный формат результата FIFO: {}", fifoResult);
            throw new IllegalStateException("Ошибка расчета налогов: неверный формат данных");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> calculation = (Map<String, Object>) calcObj;

        TaxCalculationResultDto result = new TaxCalculationResultDto();

        result.setTaxYear(taxYear);
        result.setTotalIncome((BigDecimal) calculation.getOrDefault("totalIncome", BigDecimal.ZERO));
        result.setTotalExpenses((BigDecimal) calculation.getOrDefault("totalExpenses", BigDecimal.ZERO));
        result.setTaxableProfit((BigDecimal) calculation.getOrDefault("taxableProfit", BigDecimal.ZERO));
        result.setTaxAmount((BigDecimal) calculation.getOrDefault("taxAmount", BigDecimal.ZERO));
        result.setTransactionCount(transactions.size());
        result.setCurrency("RUSSIA".equalsIgnoreCase(country) ? "RUB" : "BYN");
        result.setCountry(country);
        result.setCalculationMethod("FIFO");

        // Отправляем уведомление (не критично, если не получится)
        try {
            notificationService.notifyTaxCalculationReady(userId, taxYear, result.getTaxAmount().toPlainString());
        } catch (Exception e) {
            log.warn("⚠️ Не удалось отправить уведомление: {}", e.getMessage());
        }

        log.info("✅ Расчет налогов завершен: страна={}, налог={}", country, result.getTaxAmount());
        return result;
    }

    private String getTaxCountry(Long userId) {
        try {
            TaxProfile profile = taxProfileRepository.findByUserId(userId).orElse(null);
            return (profile != null && profile.getCountry() != null)
                    ? profile.getCountry().name()
                    : "RUSSIA";
        } catch (Exception e) {
            log.warn("⚠️ Не удалось получить налоговый профиль, используем Россию по умолчанию");
            return "RUSSIA";
        }
    }



}
