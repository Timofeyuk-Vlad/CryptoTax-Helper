package com.cryptotax.helper.service.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinGeckoService {

    private final WebClient webClient;

    private static final String COINGECKO_API_BASE = "https://api.coingecko.com/api/v3";

    /**
     * Получает текущую цену криптовалюты в USD
     */
    public BigDecimal getCurrentPrice(String cryptoId, String vsCurrency) {
        try {
            log.info("Получение текущей цены для {}/{}", cryptoId, vsCurrency);

            Map<String, Object> response = webClient.get()
                    .uri(COINGECKO_API_BASE + "/simple/price?ids={cryptoId}&vs_currencies={vsCurrency}",
                            cryptoId, vsCurrency)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey(cryptoId)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cryptoData = (Map<String, Object>) response.get(cryptoId);
                if (cryptoData.containsKey(vsCurrency)) {
                    BigDecimal price = new BigDecimal(cryptoData.get(vsCurrency).toString());
                    log.info("Текущая цена {}/{}: {}", cryptoId, vsCurrency, price);
                    return price;
                }
            }

            log.warn("Не удалось получить цену для {}/{}", cryptoId, vsCurrency);
            return BigDecimal.ZERO;

        } catch (Exception e) {
            log.error("Ошибка при получении цены с CoinGecko: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Получает историческую цену на определенную дату
     */
    public BigDecimal getHistoricalPrice(String cryptoId, String vsCurrency, LocalDate date) {
        try {
            log.info("Получение исторической цены для {}/{} на {}", cryptoId, vsCurrency, date);

            String dateStr = date.toString(); // формат YYYY-MM-DD

            Map<String, Object> response = webClient.get()
                    .uri(COINGECKO_API_BASE + "/coins/{cryptoId}/history?date={date}&localization=false",
                            cryptoId, dateStr)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("market_data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> marketData = (Map<String, Object>) response.get("market_data");

                @SuppressWarnings("unchecked")
                Map<String, Object> currentPrice = (Map<String, Object>) marketData.get("current_price");

                if (currentPrice.containsKey(vsCurrency.toLowerCase())) {
                    BigDecimal price = new BigDecimal(currentPrice.get(vsCurrency.toLowerCase()).toString());
                    log.info("Историческая цена {}/{} на {}: {}", cryptoId, vsCurrency, date, price);
                    return price;
                }
            }

            log.warn("Не удалось получить историческую цену для {}/{} на {}", cryptoId, vsCurrency, date);
            return BigDecimal.ZERO;

        } catch (Exception e) {
            log.error("Ошибка при получении исторической цены с CoinGecko: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Получает список популярных криптовалют
     */
    public Map<String, Object> getTopCryptocurrencies(int limit) {
        try {
            log.info("Получение топ-{} криптовалют", limit);

            Object[] response = webClient.get()
                    .uri(COINGECKO_API_BASE + "/coins/markets?vs_currency=usd&order=market_cap_desc&per_page={limit}&page=1",
                            limit)
                    .retrieve()
                    .bodyToMono(Object[].class)
                    .block();

            return Map.of(
                    "success", true,
                    "data", response != null ? response : new Object[0],
                    "count", response != null ? response.length : 0
            );

        } catch (Exception e) {
            log.error("Ошибка при получении списка криптовалют: {}", e.getMessage());
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * Конвертирует сумму из одной валюты в другую
     */
    public BigDecimal convertCurrency(String fromCryptoId, BigDecimal amount, String toCurrency) {
        try {
            BigDecimal price = getCurrentPrice(fromCryptoId, toCurrency);
            return amount.multiply(price);

        } catch (Exception e) {
            log.error("Ошибка конвертации: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}