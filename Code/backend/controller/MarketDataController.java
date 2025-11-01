package com.cryptotax.helper.controller;

import com.cryptotax.helper.service.exchange.CoinGeckoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarketDataController {

    private final CoinGeckoService coinGeckoService;

    @GetMapping("/price/current")
    public ResponseEntity<?> getCurrentPrice(
            @RequestParam String cryptoId,
            @RequestParam(defaultValue = "usd") String currency) {

        try {
            BigDecimal price = coinGeckoService.getCurrentPrice(cryptoId, currency);

            Map<String, Object> response = new HashMap<>();
            response.put("cryptoId", cryptoId);
            response.put("currency", currency.toUpperCase());
            response.put("price", price);
            response.put("success", price.compareTo(BigDecimal.ZERO) > 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка получения цены: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/price/historical")
    public ResponseEntity<?> getHistoricalPrice(
            @RequestParam String cryptoId,
            @RequestParam String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            BigDecimal price = coinGeckoService.getHistoricalPrice(cryptoId, currency, date);

            Map<String, Object> response = new HashMap<>();
            response.put("cryptoId", cryptoId);
            response.put("currency", currency.toUpperCase());
            response.put("date", date);
            response.put("price", price);
            response.put("success", price.compareTo(BigDecimal.ZERO) > 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка получения исторической цены: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/cryptocurrencies")
    public ResponseEntity<?> getTopCryptocurrencies(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            Map<String, Object> result = coinGeckoService.getTopCryptocurrencies(limit);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка получения списка криптовалют: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/convert")
    public ResponseEntity<?> convertCurrency(
            @RequestParam String fromCryptoId,
            @RequestParam BigDecimal amount,
            @RequestParam String toCurrency) {

        try {
            BigDecimal convertedAmount = coinGeckoService.convertCurrency(fromCryptoId, amount, toCurrency);

            Map<String, Object> response = new HashMap<>();
            response.put("from", fromCryptoId);
            response.put("amount", amount);
            response.put("to", toCurrency.toUpperCase());
            response.put("convertedAmount", convertedAmount);
            response.put("success", convertedAmount.compareTo(BigDecimal.ZERO) > 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка конвертации: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}