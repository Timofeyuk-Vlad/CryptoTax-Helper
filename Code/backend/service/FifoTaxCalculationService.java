package com.cryptotax.helper.service;

import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FifoTaxCalculationService {

    /**
     * Основной FIFO-расчёт с фильтром по валюте и очисткой нулевых данных
     */
    public Map<String, Object> calculateFifoTaxes(
            List<Transaction> transactions,
            String country,
            int taxYear,
            String filterAsset
    ) {
        log.info("🚀 Запуск FIFO расчёта для страны={}, год={}, фильтр={}",
                country, taxYear, (filterAsset != null ? filterAsset : "—нет—"));

        Map<String, Object> result = new HashMap<>();

        if (transactions == null || transactions.isEmpty()) {
            log.warn("⚠️ Пустой список транзакций, возвращаем заглушку");
            result.put("success", false);
            result.put("message", "Нет транзакций для расчёта");
            return result;
        }

        // Очистка нулевых и мусорных записей
        transactions = transactions.stream()
                .filter(tx -> tx.getBaseAsset() != null && !tx.getBaseAsset().isBlank())
                .filter(tx -> tx.getType() != null)
                .filter(tx -> tx.getAmount() != null && tx.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .filter(tx -> tx.getPrice() != null && tx.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .filter(tx -> tx.getTotal() != null && tx.getTotal().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (transactions.isEmpty()) {
            log.warn("⚠️ После фильтрации не осталось корректных транзакций");
            result.put("success", false);
            result.put("message", "Нет корректных транзакций для расчёта");
            return result;
        }

        // Фильтрация по активу
        if (filterAsset != null && !filterAsset.isBlank()) {
            String assetUpper = filterAsset.trim().toUpperCase();
            transactions = transactions.stream()
                    .filter(tx -> tx.getBaseAsset() != null && tx.getBaseAsset().equalsIgnoreCase(assetUpper))
                    .collect(Collectors.toList());
            log.info("🔎 Применён фильтр по активу: {} (осталось {} транзакций)", assetUpper, transactions.size());
        } else {
            log.info("⚙️ Фильтр валюты не задан — расчёт по всем активам");
        }

        if (transactions.isEmpty()) {
            result.put("success", false);
            result.put("message", "Нет транзакций для указанного актива");
            return result;
        }

        // Сортируем по времени
        transactions.sort(Comparator.comparing(Transaction::getTimestamp));

        // Выполняем FIFO
        Map<String, Object> fifoCalc = applySimplifiedFifo(transactions, country);

        result.put("success", true);
        result.put("taxYear", taxYear);
        result.put("country", country);
        result.put("filterAsset", filterAsset);
        result.put("fifoCalculation", fifoCalc);
        result.put("transactionCount", transactions.size());
        result.put("calculationTime", LocalDateTime.now());

        log.info("✅ FIFO расчёт завершён успешно: актив={} транзакций={}",
                (filterAsset != null ? filterAsset : "ALL"), transactions.size());
        return result;
    }

    /**
     * Упрощённый FIFO-алгоритм
     */
    private Map<String, Object> applySimplifiedFifo(List<Transaction> transactions, String country) {
        Map<String, Object> result = new HashMap<>();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        Map<String, List<Map<String, Object>>> assetDetails = new HashMap<>();

        // Группировка по активам
        Map<String, List<Transaction>> byAsset = new HashMap<>();
        for (Transaction tx : transactions) {
            byAsset.computeIfAbsent(tx.getBaseAsset(), k -> new ArrayList<>()).add(tx);
        }

        for (Map.Entry<String, List<Transaction>> entry : byAsset.entrySet()) {
            String asset = entry.getKey();
            List<Transaction> assetTxs = entry.getValue();

            List<Transaction> buys = assetTxs.stream()
                    .filter(tx -> tx.getType() == TransactionType.BUY)
                    .sorted(Comparator.comparing(Transaction::getTimestamp))
                    .collect(Collectors.toList());

            List<Transaction> sells = assetTxs.stream()
                    .filter(tx -> tx.getType() == TransactionType.SELL)
                    .sorted(Comparator.comparing(Transaction::getTimestamp))
                    .collect(Collectors.toList());

            List<Map<String, Object>> operations = new ArrayList<>();

            for (Transaction sell : sells) {
                if (buys.isEmpty()) continue;

                Transaction buy = buys.get(0);
                BigDecimal saleRevenue = sell.getTotal();
                BigDecimal buyCost = buy.getTotal();

                BigDecimal proportion = BigDecimal.ZERO;
                if (buy.getAmount() != null && buy.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    proportion = sell.getAmount().divide(buy.getAmount(), 8, RoundingMode.HALF_UP);
                }

                BigDecimal proportionalCost = buyCost.multiply(proportion);
                BigDecimal profit = saleRevenue.subtract(proportionalCost);
                if (profit.compareTo(BigDecimal.ZERO) < 0) profit = BigDecimal.ZERO;

                totalIncome = totalIncome.add(saleRevenue);
                totalExpenses = totalExpenses.add(proportionalCost);
                totalFees = totalFees.add(
                        sell.getFee() != null ? sell.getFee() : BigDecimal.ZERO
                );

                operations.add(Map.of(
                        "saleDate", sell.getTimestamp(),
                        "purchaseDate", buy.getTimestamp(),
                        "saleAmount", sell.getAmount(),
                        "salePrice", sell.getPrice(),
                        "purchasePrice", buy.getPrice(),
                        "profit", profit
                ));
            }

            assetDetails.put(asset, operations);
        }

        BigDecimal taxableProfit = totalIncome.subtract(totalExpenses).subtract(totalFees).max(BigDecimal.ZERO);
        BigDecimal tax = calculateTaxByCountry(taxableProfit, country);

        result.put("totalIncome", totalIncome.setScale(2, RoundingMode.HALF_UP));
        result.put("totalExpenses", totalExpenses.setScale(2, RoundingMode.HALF_UP));
        result.put("totalFees", totalFees.setScale(2, RoundingMode.HALF_UP));
        result.put("taxableProfit", taxableProfit.setScale(2, RoundingMode.HALF_UP));
        result.put("taxAmount", tax.setScale(2, RoundingMode.HALF_UP));
        result.put("assetDetails", assetDetails);
        result.put("currency", "RUB");

        return result;
    }

    private BigDecimal calculateTaxByCountry(BigDecimal profit, String country) {
        if (profit.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        switch (country.toUpperCase()) {
            case "RUSSIA":
            case "RU":
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
                BigDecimal taxFree = new BigDecimal("10000");
                BigDecimal taxable = profit.subtract(taxFree).max(BigDecimal.ZERO);
                return taxable.multiply(new BigDecimal("0.13"));

            default:
                return profit.multiply(new BigDecimal("0.13"));
        }
    }
}
