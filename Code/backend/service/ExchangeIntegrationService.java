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

    public List<Transaction> importFromExchange(Long connectionId, User user) {
        ExchangeConnection connection = exchangeConnectionService.getConnectionById(connectionId);

        if (!connection.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Доступ запрещен к этому подключению");
        }

        List<Transaction> transactions;

        try {
            switch (connection.getExchange()) {
                case BINANCE:
                    transactions = binanceService.importTrades(connection, user);
                    break;
                case BYBIT:
                case OKX:
                case HUOBI:
                case KUCOIN:
                    throw new RuntimeException("Интеграция с " + connection.getExchange().getDisplayName() + " пока не реализована");
                default:
                    throw new RuntimeException("Неподдерживаемая биржа: " + connection.getExchange());
            }

            // Сохраняем транзакции в базу
            List<Transaction> savedTransactions = transactionRepository.saveAll(transactions);

            // Обновляем время последней синхронизации
            exchangeConnectionService.updateLastSync(connectionId);

            // Отправляем уведомление об успешном импорте
            notificationService.notifyImportSuccess(user.getId(), savedTransactions.size(), connection.getExchange().getDisplayName());

            log.info("Успешно импортировано {} транзакций с {}", savedTransactions.size(), connection.getExchange());
            return savedTransactions;

        } catch (Exception e) {
            // Отправляем уведомление об ошибке
            notificationService.notifyImportError(user.getId(), connection.getExchange().getDisplayName(), e.getMessage());
            throw e;
        }
    }

    public boolean validateExchangeConnection(Exchange exchange, String apiKey, String apiSecret) {
        switch (exchange) {
            case BINANCE:
                return binanceService.validateApiKeys(apiKey, apiSecret);
            case BYBIT:
            case OKX:
            case HUOBI:
            case KUCOIN:
                // TODO: Реализовать валидацию для других бирж
                log.warn("Валидация для {} пока не реализована, возвращаем true", exchange);
                return true;
            default:
                throw new RuntimeException("Неподдерживаемая биржа: " + exchange);
        }
    }
}