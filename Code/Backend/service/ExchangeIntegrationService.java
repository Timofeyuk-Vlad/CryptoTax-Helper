package com.cryptotax.helper.service;

import com.cryptotax.helper.entity.Exchange;
import com.cryptotax.helper.entity.ExchangeConnection;
import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.TransactionRepository;
import com.cryptotax.helper.service.exchange.BinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeIntegrationService {

    private final BinanceService binanceService;
    private final TransactionRepository transactionRepository;
    private final ExchangeConnectionService exchangeConnectionService;
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    public List<Transaction> importFromExchange(Long connectionId, User user) {
        // ✅ В ДЕМО-РЕЖИМЕ ПРОПУСКАЕМ ПРОВЕРКУ ЛИМИТОВ
        // subscriptionService.checkTransactionImportLimit(user.getId());

        ExchangeConnection connection = exchangeConnectionService.getConnectionById(connectionId);

        if (!connection.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Доступ запрещен к этому подключению");
        }

        List<Transaction> transactions;

        try {
            log.info("Демо-режим: импорт с биржи {}", connection.getExchange());

            switch (connection.getExchange()) {
                case BINANCE:
                    transactions = binanceService.importTrades(connection, user);
                    break;
                case BYBIT:
                case OKX:
                case HUOBI:
                case KUCOIN:
                    // ✅ В ДЕМО-РЕЖИМЕ ВСЕ БИРЖИ РАБОТАЮТ КАК BINANCE
                    log.info("Демо-режим: использование Binance для {}", connection.getExchange());
                    transactions = binanceService.importTrades(connection, user);
                    break;
                default:
                    throw new RuntimeException("Неподдерживаемая биржа: " + connection.getExchange());
            }

            // Сохраняем транзакции в базу
            List<Transaction> savedTransactions = transactionRepository.saveAll(transactions);

            // Обновляем время последней синхронизации
            exchangeConnectionService.updateLastSync(connectionId);

            // Отправляем уведомление об успешном импорте
            notificationService.notifyImportSuccess(user.getId(), savedTransactions.size(), connection.getExchange().getDisplayName());

            log.info("Демо-режим: успешно импортировано {} транзакций с {}", savedTransactions.size(), connection.getExchange());
            return savedTransactions;

        } catch (Exception e) {
            // Отправляем уведомление об ошибке
            notificationService.notifyImportError(user.getId(), connection.getExchange().getDisplayName(), e.getMessage());
            throw e;
        }
    }

    public boolean validateExchangeConnection(Exchange exchange, String apiKey, String apiSecret) {
        log.info("Демо-режим: валидация подключения к {}", exchange);

        // ✅ В ДЕМО-РЕЖИМЕ ВСЕ БИРЖИ ВАЛИДИРУЮТСЯ ЧЕРЕЗ BINANCE SERVICE
        switch (exchange) {
            case BINANCE:
                return binanceService.validateApiKeys(apiKey, apiSecret);
            case BYBIT:
            case OKX:
            case HUOBI:
            case KUCOIN:
                // ✅ В ДЕМО-РЕЖИМЕ ВСЕ БИРЖИ РАБОТАЮТ
                log.info("Демо-режим: успешная валидация для {}", exchange);
                return binanceService.validateApiKeys(apiKey, apiSecret);
            default:
                throw new RuntimeException("Неподдерживаемая биржа: " + exchange);
        }
    }

    /**
     * Демо-метод для быстрого создания тестовых данных
     */
    public List<Transaction> createDemoTransactions(User user) {
        try {
            log.info("Создание демо-транзакций для пользователя {}", user.getEmail());
            return binanceService.importDemoWithRealPrices(user);

        } catch (Exception e) {
            log.error("Ошибка создания демо-транзакций: {}", e.getMessage());
            throw new RuntimeException("Ошибка создания демо-данных: " + e.getMessage());
        }
    }
}