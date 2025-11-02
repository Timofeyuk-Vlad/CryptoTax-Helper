package com.cryptotax.helper.service;

import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class FifoTaxCalculationService {

    /**
     * Упрощенный FIFO расчет для демонстрации
     */
    public Map<String, Object> calculateFifoTaxes(List<Transaction> transactions, String country, int taxYear) {
        log.info("Демо-режим: FIFO расчет для {}, год {}", country, taxYear);

        Map<String, Object> result = new HashMap<>();

        if (transactions.isEmpty()) {
            // Если нет транзакций, создаем реалистичные демо-данные
            transactions = generateDemoTransactions();
        }

        // Сортируем транзакции по времени (FIFO требует хронологический порядок)
        transactions.sort(Comparator.comparing(Transaction::getTimestamp));

        // Применяем "упрощенный FIFO"
        Map<String, Object> fifoResult = applySimplifiedFifo(transactions, country);

        result.put("success", true);
        result.put("taxYear", taxYear);
        result.put("country", country);
        result.put("calculationMethod", "FIFO");
        result.put("transactionCount", transactions.size());
        result.put("fifoCalculation", fifoResult);
        result.put("demoMode", true);
        result.put("calculationTime", LocalDateTime.now());

        log.info("Демо FIFO расчет завершен: {}", result);
        return result;
    }

    /**
     * Упрощенный FIFO для демонстрации
     */
    private Map<String, Object> applySimplifiedFifo(List<Transaction> transactions, String country) {
        Map<String, Object> result = new HashMap<>();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        Map<String, List<Map<String, Object>>> assetCalculations = new HashMap<>();

        // Группируем по активам для "FIFO-like" расчета
        Map<String, List<Transaction>> transactionsByAsset = new HashMap<>();
        for (Transaction tx : transactions) {
            transactionsByAsset
                    .computeIfAbsent(tx.getBaseAsset(), k -> new ArrayList<>())
                    .add(tx);
        }

        // "FIFO расчет" по каждому активу
        for (Map.Entry<String, List<Transaction>> entry : transactionsByAsset.entrySet()) {
            String asset = entry.getKey();
            List<Transaction> assetTransactions = entry.getValue();

            List<Map<String, Object>> fifoOperations = new ArrayList<>();
            BigDecimal assetProfit = BigDecimal.ZERO;
            BigDecimal assetCost = BigDecimal.ZERO;

            // Упрощенный FIFO: покупки -> продажи
            List<Transaction> purchases = assetTransactions.stream()
                    .filter(tx -> tx.getType() == TransactionType.BUY)
                    .sorted(Comparator.comparing(Transaction::getTimestamp))
                    .toList();

            List<Transaction> sales = assetTransactions.stream()
                    .filter(tx -> tx.getType() == TransactionType.SELL)
                    .sorted(Comparator.comparing(Transaction::getTimestamp))
                    .toList();

            // Демо-расчет прибыли
            for (Transaction sale : sales) {
                if (!purchases.isEmpty()) {
                    // Берем первую покупку (FIFO)
                    Transaction firstPurchase = purchases.get(0);

                    BigDecimal saleRevenue = sale.getTotal() != null ? sale.getTotal() : BigDecimal.ZERO;
                    BigDecimal purchaseCost = firstPurchase.getTotal() != null ? firstPurchase.getTotal() : BigDecimal.ZERO;
                    BigDecimal profit = saleRevenue.subtract(purchaseCost).max(BigDecimal.ZERO);

                    assetProfit = assetProfit.add(profit);
                    assetCost = assetCost.add(purchaseCost);
                    totalIncome = totalIncome.add(saleRevenue);

                    fifoOperations.add(Map.of(
                            "saleDate", sale.getTimestamp(),
                            "saleAmount", sale.getAmount(),
                            "salePrice", sale.getPrice(),
                            "purchaseDate", firstPurchase.getTimestamp(),
                            "purchasePrice", firstPurchase.getPrice(),
                            "profit", profit
                    ));
                }
            }

            // Учитываем расходы на покупки
            for (Transaction purchase : purchases) {
                if (purchase.getTotal() != null) {
                    totalExpenses = totalExpenses.add(purchase.getTotal());
                }
            }

            // Учитываем комиссии
            for (Transaction tx : assetTransactions) {
                if (tx.getFee() != null) {
                    totalFees = totalFees.add(tx.getFee());
                    totalExpenses = totalExpenses.add(tx.getFee());
                }
            }

            assetCalculations.put(asset, fifoOperations);
        }

        BigDecimal taxableProfit = totalIncome.subtract(totalExpenses).max(BigDecimal.ZERO);
        BigDecimal taxAmount = calculateTaxByCountry(taxableProfit, country);

        result.put("totalIncome", totalIncome.setScale(2, RoundingMode.HALF_UP));
        result.put("totalExpenses", totalExpenses.setScale(2, RoundingMode.HALF_UP));
        result.put("totalFees", totalFees.setScale(2, RoundingMode.HALF_UP));
        result.put("taxableProfit", taxableProfit.setScale(2, RoundingMode.HALF_UP));
        result.put("taxAmount", taxAmount.setScale(2, RoundingMode.HALF_UP));
        result.put("assetCalculations", assetCalculations);
        result.put("currency", "RUB");

        return result;
    }

    /**
     * Расчет налога по стране
     */
    private BigDecimal calculateTaxByCountry(BigDecimal profit, String country) {
        if (profit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        switch (country.toUpperCase()) {
            case "RUSSIA":
            case "RU":
                // РФ: 13% до 2.4 млн, 15% свыше
                BigDecimal threshold = new BigDecimal("2400000");
                if (profit.compareTo(threshold) <= 0) {
                    return profit.multiply(new BigDecimal("0.13"));
                } else {
                    BigDecimal taxUnder = threshold.multiply(new BigDecimal("0.13"));
                    BigDecimal taxOver = profit.subtract(threshold).multiply(new BigDecimal("0.15"));
                    return taxUnder.add(taxOver);
                }

            case "BELARUS":
            case "BY":
                // РБ: 13% с учетом необлагаемого минимума 10,000 BYN
                BigDecimal taxFreeAllowance = new BigDecimal("10000");
                BigDecimal taxableAmount = profit.subtract(taxFreeAllowance).max(BigDecimal.ZERO);
                return taxableAmount.multiply(new BigDecimal("0.13"));

            default:
                return profit.multiply(new BigDecimal("0.13"));
        }
    }

    /**
     * Генерация демо-транзакций если нет реальных
     */
    private List<Transaction> generateDemoTransactions() {
        List<Transaction> demoTransactions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Демо-покупки
        demoTransactions.add(createDemoTransaction("BTC", new BigDecimal("0.5"), TransactionType.BUY,
                new BigDecimal("40000"), now.minusMonths(6)));
        demoTransactions.add(createDemoTransaction("ETH", new BigDecimal("3.0"), TransactionType.BUY,
                new BigDecimal("2500"), now.minusMonths(4)));
        demoTransactions.add(createDemoTransaction("BTC", new BigDecimal("0.2"), TransactionType.BUY,
                new BigDecimal("45000"), now.minusMonths(2)));

        // Демо-продажи (для FIFO)
        demoTransactions.add(createDemoTransaction("BTC", new BigDecimal("0.3"), TransactionType.SELL,
                new BigDecimal("52000"), now.minusMonths(1)));
        demoTransactions.add(createDemoTransaction("ETH", new BigDecimal("1.5"), TransactionType.SELL,
                new BigDecimal("3200"), now.minusWeeks(2)));

        return demoTransactions;
    }

    private Transaction createDemoTransaction(String asset, BigDecimal amount, TransactionType type,
                                              BigDecimal price, LocalDateTime timestamp) {
        Transaction tx = new Transaction();
        tx.setBaseAsset(asset);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setPrice(price);
        tx.setTotal(price.multiply(amount));
        tx.setTimestamp(timestamp);
        tx.setFee(price.multiply(amount).multiply(new BigDecimal("0.001"))); // 0.1% комиссия
        tx.setFeeAsset("BNB");
        return tx;
    }
}