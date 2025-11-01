package com.cryptotax.helper.service.exchange;

import com.cryptotax.helper.dto.exchange.BinanceTradeDto;
import com.cryptotax.helper.entity.ExchangeConnection;
import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.TransactionType;
import com.cryptotax.helper.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceService {

    private final CoinGeckoService coinGeckoService;

    /**
     * Основной метод импорта - теперь работает в демо-режиме с реальными ценами
     */
    public List<Transaction> importTrades(ExchangeConnection connection, User user) {
        try {
            log.info("Демо-импорт транзакций с реальными ценами для пользователя {}", user.getEmail());

            // В демо-режиме игнорируем реальные API ключи и создаем реалистичные данные
            List<Transaction> transactions = createRealisticDemoTransactions(user, connection);

            log.info("Успешно создано {} демо-транзакций с реальными ценами", transactions.size());
            return transactions;

        } catch (Exception e) {
            log.error("Ошибка при демо-импорте транзакций: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка демо-импорта: " + e.getMessage(), e);
        }
    }

    /**
     * Валидация API ключей - в демо-режиме всегда возвращает true для любых ключей
     */
    public boolean validateApiKeys(String apiKey, String apiSecret) {
        log.info("Демо-режим: валидация API ключей Binance");

        // ✅ В ДЕМО-РЕЖИМЕ ПРИНИМАЕМ ЛЮБЫЕ КЛЮЧИ КРОМЕ ПУСТЫХ
        if (apiKey == null || apiKey.trim().isEmpty() || apiSecret == null || apiSecret.trim().isEmpty()) {
            log.warn("Пустые API ключи в демо-режиме");
            return false;
        }

        // ✅ ПРИНИМАЕМ ЛЮБЫЕ НЕПУСТЫЕ КЛЮЧИ ДАЖЕ КОРОТКИЕ
        log.info("Демо-режим: успешная валидация для ключа: {}...",
                apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : apiKey);
        return true;
    }

    /**
     * Создание реалистичных демо-транзакций с реальными ценами
     */
    private List<Transaction> createRealisticDemoTransactions(User user, ExchangeConnection connection) {
        List<Transaction> transactions = new ArrayList<>();
        Random random = new Random();

        // Получаем текущие реальные цены
        Map<String, BigDecimal> currentPrices = getCurrentRealPrices();

        // Создаем историю транзакций за последние 6 месяцев
        LocalDateTime now = LocalDateTime.now();

        // 1. Покупка BTC 6 месяцев назад
        transactions.add(createHistoricalTransaction(
                user, connection, "BTC", new BigDecimal("0.5"), TransactionType.BUY,
                now.minusMonths(6), currentPrices.get("BTC").multiply(new BigDecimal("0.7")), // Цена 6 месяцев назад
                random
        ));

        // 2. Покупка ETH 4 месяца назад
        transactions.add(createHistoricalTransaction(
                user, connection, "ETH", new BigDecimal("3.0"), TransactionType.BUY,
                now.minusMonths(4), currentPrices.get("ETH").multiply(new BigDecimal("0.8")),
                random
        ));

        // 3. Покупка ADA 3 месяца назад
        transactions.add(createHistoricalTransaction(
                user, connection, "ADA", new BigDecimal("500"), TransactionType.BUY,
                now.minusMonths(3), currentPrices.get("ADA").multiply(new BigDecimal("0.85")),
                random
        ));

        // 4. Продажа части BTC 2 месяца назад
        transactions.add(createHistoricalTransaction(
                user, connection, "BTC", new BigDecimal("0.1"), TransactionType.SELL,
                now.minusMonths(2), currentPrices.get("BTC").multiply(new BigDecimal("1.2")),
                random
        ));

        // 5. Покупка SOL 1 месяц назад
        transactions.add(createHistoricalTransaction(
                user, connection, "SOL", new BigDecimal("10"), TransactionType.BUY,
                now.minusMonths(1), currentPrices.get("SOL").multiply(new BigDecimal("0.9")),
                random
        ));

        // 6. Стейкинг награда ADA
        transactions.add(createStakingTransaction(
                user, connection, "ADA", new BigDecimal("25"),
                now.minusDays(15), currentPrices.get("ADA")
        ));

        // 7. Недавняя покупка DOT
        transactions.add(createHistoricalTransaction(
                user, connection, "DOT", new BigDecimal("50"), TransactionType.BUY,
                now.minusDays(7), currentPrices.get("DOT").multiply(new BigDecimal("0.95")),
                random
        ));

        // Сортируем по времени (старые сначала)
        transactions.sort(Comparator.comparing(Transaction::getTimestamp));

        return transactions;
    }

    /**
     * Создание исторической транзакции с реалистичными данными
     */
    private Transaction createHistoricalTransaction(User user, ExchangeConnection connection,
                                                    String asset, BigDecimal amount, TransactionType type,
                                                    LocalDateTime timestamp, BigDecimal historicalPrice,
                                                    Random random) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setExchangeConnection(connection);
        transaction.setBaseAsset(asset);
        transaction.setQuoteAsset("USDT");
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setTimestamp(timestamp);
        transaction.setImportedAt(LocalDateTime.now());
        transaction.setIsProcessed(true);

        // Генерируем реалистичный ID транзакции
        transaction.setExchangeTxId("BINANCE_DEMO_" + System.currentTimeMillis() + "_" +
                asset + "_" + type.name() + "_" + random.nextInt(10000));

        // Используем историческую цену с небольшими случайными отклонениями для реалистичности
        BigDecimal priceVariation = historicalPrice.multiply(
                BigDecimal.valueOf(0.95 + random.nextDouble() * 0.1) // ±5% вариация
        ).setScale(2, RoundingMode.HALF_UP);

        transaction.setPrice(priceVariation);

        // Рассчитываем общую сумму
        BigDecimal total = priceVariation.multiply(amount).setScale(2, RoundingMode.HALF_UP);
        transaction.setTotal(total);

        // Добавляем комиссию (0.1% от суммы)
        BigDecimal fee = total.multiply(new BigDecimal("0.001")).setScale(6, RoundingMode.HALF_UP);
        transaction.setFee(fee);
        transaction.setFeeAsset(asset.equals("BTC") ? "BTC" : "BNB");

        log.debug("Создана демо-транзакция: {} {} {} по цене {}",
                type.getDisplayName(), amount, asset, priceVariation);

        return transaction;
    }

    /**
     * Создание транзакции стейкинга
     */
    private Transaction createStakingTransaction(User user, ExchangeConnection connection,
                                                 String asset, BigDecimal rewardAmount,
                                                 LocalDateTime timestamp, BigDecimal currentPrice) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setExchangeConnection(connection);
        transaction.setBaseAsset(asset);
        transaction.setType(TransactionType.STAKING);
        transaction.setAmount(rewardAmount);
        transaction.setTimestamp(timestamp);
        transaction.setImportedAt(LocalDateTime.now());
        transaction.setIsProcessed(true);
        transaction.setExchangeTxId("BINANCE_STAKING_" + System.currentTimeMillis() + "_" + asset);

        // Для стейкинга цена и общая сумма не обязательны, но можем установить для расчетов
        transaction.setPrice(currentPrice);
        transaction.setTotal(currentPrice.multiply(rewardAmount).setScale(2, RoundingMode.HALF_UP));

        log.debug("Создана демо-транзакция стейкинга: {} {}", rewardAmount, asset);

        return transaction;
    }

    /**
     * Получение текущих реальных цен через CoinGecko
     */
    private Map<String, BigDecimal> getCurrentRealPrices() {
        Map<String, BigDecimal> prices = new HashMap<>();

        try {
            prices.put("BTC", coinGeckoService.getCurrentPrice("bitcoin", "usd"));
            prices.put("ETH", coinGeckoService.getCurrentPrice("ethereum", "usd"));
            prices.put("ADA", coinGeckoService.getCurrentPrice("cardano", "usd"));
            prices.put("SOL", coinGeckoService.getCurrentPrice("solana", "usd"));
            prices.put("DOT", coinGeckoService.getCurrentPrice("polkadot", "usd"));
            prices.put("BNB", coinGeckoService.getCurrentPrice("binancecoin", "usd"));

            log.info("Получены реальные цены: BTC=${}, ETH=${}, ADA=${}",
                    prices.get("BTC"), prices.get("ETH"), prices.get("ADA"));

        } catch (Exception e) {
            log.warn("Не удалось получить реальные цены, используем демо-значения: {}", e.getMessage());

            // Демо-значения если CoinGecko недоступен
            prices.put("BTC", new BigDecimal("45000.00"));
            prices.put("ETH", new BigDecimal("3200.00"));
            prices.put("ADA", new BigDecimal("0.45"));
            prices.put("SOL", new BigDecimal("95.00"));
            prices.put("DOT", new BigDecimal("6.50"));
            prices.put("BNB", new BigDecimal("350.00"));
        }

        return prices;
    }

    /**
     * Метод для импорта по конкретному символу (демо-версия)
     */
    public List<BinanceTradeDto> getTradeHistoryBySymbol(String apiKey, String apiSecret, String symbol) {
        log.info("Демо-режим: запрос истории по символу {}", symbol);

        // В демо-режиме возвращаем пустой список
        // В реальной реализации здесь был бы вызов Binance API
        return new ArrayList<>();
    }

    /**
     * Дополнительный метод для создания демо-транзакций с реальными ценами
     */
    public List<Transaction> importDemoWithRealPrices(User user) {
        try {
            log.info("Создание демо-транзакций с реальными ценами для пользователя {}", user.getEmail());

            // Создаем временное подключение для демо
            ExchangeConnection demoConnection = new ExchangeConnection();
            demoConnection.setId(999L); // Демо ID
            demoConnection.setExchange(com.cryptotax.helper.entity.Exchange.BINANCE);

            List<Transaction> transactions = createRealisticDemoTransactions(user, demoConnection);

            log.info("Создано {} демо-транзакций с реальными ценами", transactions.size());
            return transactions;

        } catch (Exception e) {
            log.error("Ошибка создания демо-транзакций: {}", e.getMessage());
            throw new RuntimeException("Ошибка создания демо-данных: " + e.getMessage());
        }
    }
}