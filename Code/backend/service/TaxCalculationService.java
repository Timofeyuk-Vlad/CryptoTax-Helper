package com.cryptotax.helper.service;

import com.cryptotax.helper.dto.TaxCalculationResultDto;
import com.cryptotax.helper.entity.TaxProfile;
import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.TaxProfileRepository;
import com.cryptotax.helper.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public TaxCalculationResultDto calculateTaxes(Long userId, int taxYear) {
        User user = new User();
        user.setId(userId);

        log.info("Расчет налогов для пользователя {} за {} год", userId, taxYear);

        // Получаем налоговый профиль для определения страны
        String country = getTaxCountry(userId);

        // Получаем транзакции за год
        List<Transaction> transactions = transactionRepository.findByUserAndYearOrderByTimestampDesc(user, taxYear);

        // Используем FIFO расчет
        Map<String, Object> fifoResult = fifoService.calculateFifoTaxes(transactions, country, taxYear);

        // Конвертируем в DTO
        TaxCalculationResultDto result = convertToTaxResultDto(fifoResult, taxYear);
        result.setTransactionCount(transactions.size());

        // Уведомление
        try {
            notificationService.notifyTaxCalculationReady(userId, taxYear, result.getTaxAmount().toString());
        } catch (Exception e) {
            log.warn("Не удалось отправить уведомление: {}", e.getMessage());
        }

        log.info("Расчет налогов завершен: страна={}, налог={}", country, result.getTaxAmount());
        return result;
    }

    /**
     * Получение страны из налогового профиля
     */
    private String getTaxCountry(Long userId) {
        try {
            TaxProfile profile = taxProfileRepository.findByUserId(userId)
                    .orElse(null);
            return profile != null && profile.getCountry() != null ?
                    profile.getCountry().name() : "RUSSIA";
        } catch (Exception e) {
            log.warn("Не удалось получить налоговый профиль, используем Россию по умолчанию");
            return "RUSSIA";
        }
    }

    /**
     * Конвертация FIFO результата в DTO
     */
    private TaxCalculationResultDto convertToTaxResultDto(Map<String, Object> fifoResult, int taxYear) {
        TaxCalculationResultDto result = new TaxCalculationResultDto();
        result.setTaxYear(taxYear);

        Map<String, Object> calculation = (Map<String, Object>) fifoResult.get("fifoCalculation");

        result.setTotalIncome((BigDecimal) calculation.get("totalIncome"));
        result.setTotalExpenses((BigDecimal) calculation.get("totalExpenses"));
        result.setTaxableProfit((BigDecimal) calculation.get("taxableProfit"));
        result.setTaxAmount((BigDecimal) calculation.get("taxAmount"));

        // Добавляем информацию о стране и методе
        result.setCurrency(fifoResult.get("country").equals("BELARUS") ? "BYN" : "RUB");

        return result;
    }
}