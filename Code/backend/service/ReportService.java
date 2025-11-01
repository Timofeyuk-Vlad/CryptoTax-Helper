package com.cryptotax.helper.service;

import com.cryptotax.helper.dto.TaxCalculationResultDto;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final TaxCalculationService taxCalculationService;

    public Map<String, Object> generateTaxReport(Long userId, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        TaxCalculationResultDto taxResult = taxCalculationService.calculateTaxes(userId, year);

        Map<String, Object> report = new HashMap<>();

        // Заголовок отчета
        report.put("reportType", "TAX_REPORT");
        report.put("reportYear", year);
        report.put("generatedAt", LocalDateTime.now());
        report.put("currency", "RUB");

        // Информация о пользователе
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("firstName", user.getFirstName());
        userInfo.put("lastName", user.getLastName());
        userInfo.put("email", user.getEmail());
        userInfo.put("taxIdentificationNumber", "123456789012"); // Временная заглушка
        report.put("taxpayerInfo", userInfo);

        // Налоговые данные
        Map<String, Object> taxData = new HashMap<>();
        taxData.put("totalIncome", taxResult.getTotalIncome());
        taxData.put("totalExpenses", taxResult.getTotalExpenses());
        taxData.put("taxableProfit", taxResult.getTaxableProfit());
        taxData.put("taxAmount", taxResult.getTaxAmount());
        taxData.put("transactionCount", taxResult.getTransactionCount());
        report.put("taxData", taxData);

        // Расчет налога
        Map<String, Object> taxCalculation = new HashMap<>();
        taxCalculation.put("taxRate", "13%"); // Базовая ставка
        taxCalculation.put("calculationMethod", "FIFO");
        taxCalculation.put("fiscalYear", year);
        report.put("taxCalculation", taxCalculation);

        // Рекомендации
        report.put("recommendations", generateRecommendations(taxResult));

        return report;
    }

    private Map<String, Object> generateRecommendations(TaxCalculationResultDto taxResult) {
        Map<String, Object> recommendations = new HashMap<>();

        if (taxResult.getTaxAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            recommendations.put("actionRequired", true);
            recommendations.put("deadline", "30 апреля " + (taxResult.getTaxYear() + 1));
            recommendations.put("message", "Необходимо подать налоговую декларацию 3-НДФЛ");
            recommendations.put("documents", java.util.List.of(
                    "Справка о доходах от операций с цифровыми активами",
                    "Декларация 3-НДФЛ",
                    "Квитанция об уплате налога"
            ));
        } else {
            recommendations.put("actionRequired", false);
            recommendations.put("message", "Налог к уплате отсутствует");
        }

        return recommendations;
    }
}