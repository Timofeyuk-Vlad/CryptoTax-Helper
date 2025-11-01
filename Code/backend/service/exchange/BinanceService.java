package com.cryptotax.helper.service.exchange;

import com.cryptotax.helper.dto.exchange.BinanceTradeDto;
import com.cryptotax.helper.entity.ExchangeConnection;
import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.TransactionType;
import com.cryptotax.helper.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceService {

    private final WebClient webClient;

    public List<Transaction> importTrades(ExchangeConnection connection, User user) {
        try {
            log.info("Начинаем импорт транзакций с Binance для пользователя {}", user.getEmail());

            String apiKey = connection.getApiKey();
            String apiSecret = connection.getApiSecret();

            // Получаем историю торгов
            List<BinanceTradeDto> binanceTrades = getTradeHistory(apiKey, apiSecret);

            // Конвертируем в наши транзакции
            List<Transaction> transactions = convertToTransactions(binanceTrades, user, connection);

            log.info("Успешно импортировано {} транзакций с Binance", transactions.size());
            return transactions;

        } catch (Exception e) {
            log.error("Ошибка при импорте транзакций с Binance: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка импорта с Binance: " + e.getMessage(), e);
        }
    }

    private List<BinanceTradeDto> getTradeHistory(String apiKey, String apiSecret) {
        try {
            long timestamp = System.currentTimeMillis();
            String signature = generateSignature("timestamp=" + timestamp, apiSecret);

            // В реальном приложении здесь будет вызов Binance API
            // Сейчас возвращаем демо-данные
            log.info("Имитация вызова Binance API с ключом: {} и сигнатурой: {}", apiKey, signature);

            return generateDemoBinanceTrades();

        } catch (Exception e) {
            log.error("Ошибка при получении истории торгов с Binance: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения данных с Binance", e);
        }
    }

    private List<BinanceTradeDto> generateDemoBinanceTrades() {
        List<BinanceTradeDto> trades = new ArrayList<>();
        long baseTime = System.currentTimeMillis();

        // Демо данные - покупка BTC
        BinanceTradeDto buyBtc = new BinanceTradeDto();
        buyBtc.setTradeId(1L);
        buyBtc.setSymbol("BTCUSDT");
        buyBtc.setPrice("45000.00");
        buyBtc.setQuantity("0.1");
        buyBtc.setQuoteQuantity("4500.00");
        buyBtc.setCommission("4.50");
        buyBtc.setCommissionAsset("USDT");
        buyBtc.setTimestamp(baseTime - 86400000L * 30); // 30 дней назад
        buyBtc.setIsBuyer(true);
        trades.add(buyBtc);

        // Продажа BTC
        BinanceTradeDto sellBtc = new BinanceTradeDto();
        sellBtc.setTradeId(2L);
        sellBtc.setSymbol("BTCUSDT");
        sellBtc.setPrice("52000.00");
        sellBtc.setQuantity("0.05");
        sellBtc.setQuoteQuantity("2600.00");
        sellBtc.setCommission("2.60");
        sellBtc.setCommissionAsset("USDT");
        sellBtc.setTimestamp(baseTime - 86400000L * 15); // 15 дней назад
        sellBtc.setIsBuyer(false);
        trades.add(sellBtc);

        // Покупка ETH
        BinanceTradeDto buyEth = new BinanceTradeDto();
        buyEth.setTradeId(3L);
        buyEth.setSymbol("ETHUSDT");
        buyEth.setPrice("3200.00");
        buyEth.setQuantity("2.0");
        buyEth.setQuoteQuantity("6400.00");
        buyEth.setCommission("6.40");
        buyEth.setCommissionAsset("USDT");
        buyEth.setTimestamp(baseTime - 86400000L * 10); // 10 дней назад
        buyEth.setIsBuyer(true);
        trades.add(buyEth);

        return trades;
    }

    private List<Transaction> convertToTransactions(List<BinanceTradeDto> binanceTrades, User user, ExchangeConnection connection) {
        List<Transaction> transactions = new ArrayList<>();

        for (BinanceTradeDto binanceTrade : binanceTrades) {
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setExchangeConnection(connection);
            transaction.setExchangeTxId("BINANCE_" + binanceTrade.getTradeId());

            // Определяем тип транзакции
            if (binanceTrade.getIsBuyer()) {
                transaction.setType(TransactionType.BUY);
            } else {
                transaction.setType(TransactionType.SELL);
            }

            // Парсим символ (например, "BTCUSDT" -> base="BTC", quote="USDT")
            String symbol = binanceTrade.getSymbol();
            if (symbol.endsWith("USDT")) {
                transaction.setBaseAsset(symbol.substring(0, symbol.length() - 4));
                transaction.setQuoteAsset("USDT");
            } else {
                transaction.setBaseAsset(symbol.substring(0, 3));
                transaction.setQuoteAsset(symbol.substring(3));
            }

            transaction.setAmount(new BigDecimal(binanceTrade.getQuantity()));
            transaction.setPrice(new BigDecimal(binanceTrade.getPrice()));
            transaction.setTotal(new BigDecimal(binanceTrade.getQuoteQuantity()));
            transaction.setFee(new BigDecimal(binanceTrade.getCommission()));
            transaction.setFeeAsset(binanceTrade.getCommissionAsset());

            // Конвертируем timestamp в LocalDateTime
            LocalDateTime tradeTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(binanceTrade.getTimestamp()),
                    ZoneId.systemDefault()
            );
            transaction.setTimestamp(tradeTime);
            transaction.setImportedAt(LocalDateTime.now());
            transaction.setIsProcessed(true);

            transactions.add(transaction);
        }

        return transactions;
    }

    private String generateSignature(String data, String apiSecret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации подписи", e);
        }
    }

    public boolean validateApiKeys(String apiKey, String apiSecret) {
        try {
            // Простая проверка - в реальном приложении делаем тестовый запрос
            log.info("Валидация API ключей Binance...");
            return apiKey != null && !apiKey.trim().isEmpty() &&
                    apiSecret != null && !apiSecret.trim().isEmpty();

        } catch (Exception e) {
            log.error("Ошибка валидации API ключей Binance: {}", e.getMessage());
            return false;
        }
    }
}