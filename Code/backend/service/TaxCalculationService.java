package com.cryptotax.helper.service;

import com.cryptotax.helper.dto.TaxCalculationResultDto;
import com.cryptotax.helper.entity.*;
import com.cryptotax.helper.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaxCalculationService {

    private final TransactionRepository transactionRepository;

    public TaxCalculationResultDto calculateTaxes(Long userId, int taxYear) {
        // Создаем временного пользователя для запроса
        User user = new User();
        user.setId(userId);

        // Получаем транзакции за год (пока заглушка)
        List<Transaction> transactions = new ArrayList<>(); // Временно пустой список

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal taxableProfit = BigDecimal.ZERO;

        // Демо расчет - временные данные для тестирования
        totalIncome = new BigDecimal("3000");
        totalExpenses = new BigDecimal("2500");
        taxableProfit = new BigDecimal("500");

        TaxCalculationResultDto result = new TaxCalculationResultDto();
        result.setTaxYear(taxYear);
        result.setTotalIncome(totalIncome);
        result.setTotalExpenses(totalExpenses);
        result.setTaxableProfit(taxableProfit);
        result.setTransactionCount(transactions.size());

        // Расчет налога для РФ
        BigDecimal taxAmount = calculateTaxForRussia(taxableProfit);
        result.setTaxAmount(taxAmount);

        return result;
    }

    private BigDecimal calculateTaxForRussia(BigDecimal profit) {
        if (profit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // НДФЛ 13% до 2.4 млн, 15% свыше
        BigDecimal threshold = new BigDecimal("2400000");
        if (profit.compareTo(threshold) <= 0) {
            return profit.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal taxUnderThreshold = threshold.multiply(new BigDecimal("0.13"));
            BigDecimal taxOverThreshold = profit.subtract(threshold).multiply(new BigDecimal("0.15"));
            return taxUnderThreshold.add(taxOverThreshold).setScale(2, RoundingMode.HALF_UP);
        }
    }

    // Вспомогательные классы для FIFO (будут использоваться позже)
    private static class FifoQueue {
        private final List<FifoItem> items = new ArrayList<>();

        void addPurchase(BigDecimal amount, BigDecimal price, BigDecimal total) {
            items.add(new FifoItem(amount, price, total));
        }

        SellResult processSale(BigDecimal saleAmount, BigDecimal salePrice, BigDecimal saleTotal) {
            BigDecimal remainingAmount = saleAmount;
            BigDecimal totalCostBasis = BigDecimal.ZERO;

            while (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && !items.isEmpty()) {
                FifoItem firstItem = items.get(0);

                if (firstItem.amount.compareTo(remainingAmount) >= 0) {
                    BigDecimal costBasis = firstItem.price.multiply(remainingAmount);
                    totalCostBasis = totalCostBasis.add(costBasis);
                    firstItem.amount = firstItem.amount.subtract(remainingAmount);
                    remainingAmount = BigDecimal.ZERO;
                } else {
                    totalCostBasis = totalCostBasis.add(firstItem.total);
                    remainingAmount = remainingAmount.subtract(firstItem.amount);
                    items.remove(0);
                }
            }

            BigDecimal revenue = saleTotal;
            BigDecimal profit = revenue.subtract(totalCostBasis);

            return new SellResult(revenue, totalCostBasis, profit);
        }
    }

    private static class FifoItem {
        BigDecimal amount;
        BigDecimal price;
        BigDecimal total;

        FifoItem(BigDecimal amount, BigDecimal price, BigDecimal total) {
            this.amount = amount;
            this.price = price;
            this.total = total;
        }
    }

    private static class SellResult {
        private final BigDecimal revenue;
        private final BigDecimal costBasis;
        private final BigDecimal profit;

        SellResult(BigDecimal revenue, BigDecimal costBasis, BigDecimal profit) {
            this.revenue = revenue;
            this.costBasis = costBasis;
            this.profit = profit;
        }

        BigDecimal getRevenue() { return revenue; }
        BigDecimal getCostBasis() { return costBasis; }
        BigDecimal getProfit() { return profit; }
    }
}