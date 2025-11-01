package com.cryptotax.helper.service;

import com.cryptotax.helper.dto.TaxCalculationResultDto;
import com.cryptotax.helper.entity.ExchangeConnection;
import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.ExchangeConnectionRepository;
import com.cryptotax.helper.repository.TransactionRepository;
import com.cryptotax.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ExchangeConnectionRepository exchangeConnectionRepository;
    private final TaxCalculationService taxCalculationService;
    private final TransactionImportService transactionImportService;

    public Map<String, Object> getDashboardData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Map<String, Object> dashboard = new HashMap<>();

        // Основная статистика
        dashboard.put("userInfo", getUserInfo(user));
        dashboard.put("portfolioSummary", getPortfolioSummary(userId));
        dashboard.put("exchangeConnections", getExchangeConnections(user));
        dashboard.put("recentTransactions", getRecentTransactions(user));
        dashboard.put("taxOverview", getTaxOverview(userId));
        dashboard.put("quickActions", getQuickActions());

        return dashboard;
    }

    private Map<String, Object> getUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("firstName", user.getFirstName());
        userInfo.put("lastName", user.getLastName());
        userInfo.put("email", user.getEmail());
        userInfo.put("is2faEnabled", user.getIs2faEnabled());
        userInfo.put("memberSince", user.getCreatedAt().getYear());
        return userInfo;
    }

    // В методе getPortfolioSummary добавляем демо-данные:
    private Map<String, Object> getPortfolioSummary(Long userId) {
        Map<String, Object> portfolio = new HashMap<>();

        // Демо-данные портфеля
        portfolio.put("totalPortfolioValue", new BigDecimal("15420.75"));
        portfolio.put("totalInvested", new BigDecimal("12000.00"));
        portfolio.put("totalProfit", new BigDecimal("3420.75"));
        portfolio.put("profitPercentage", new BigDecimal("28.5"));
        portfolio.put("transactionCount", transactionImportService.getUserTransactionCount(userId));
        portfolio.put("connectedExchanges", 2);
        portfolio.put("demoMode", true); // ✅ ДОБАВЛЯЕМ ФЛАГ ДЕМО-РЕЖИМА

        // Демо-активы
        Map<String, Object> assets = new HashMap<>();
        assets.put("BTC", Map.of("amount", new BigDecimal("0.4"), "value", new BigDecimal("18000.00")));
        assets.put("ETH", Map.of("amount", new BigDecimal("3.0"), "value", new BigDecimal("9600.00")));
        assets.put("ADA", Map.of("amount", new BigDecimal("525"), "value", new BigDecimal("236.25")));
        assets.put("USDT", Map.of("amount", new BigDecimal("1250.00"), "value", new BigDecimal("1250.00")));
        portfolio.put("assets", assets);

        return portfolio;
    }

    private List<Map<String, Object>> getExchangeConnections(User user) {
        List<ExchangeConnection> connections = exchangeConnectionRepository.findByUserAndIsActiveTrue(user);

        return connections.stream()
                .map(connection -> {
                    Map<String, Object> connInfo = new HashMap<>();
                    connInfo.put("id", connection.getId());
                    connInfo.put("exchange", connection.getExchange());
                    connInfo.put("exchangeDisplayName", connection.getExchange().getDisplayName());
                    connInfo.put("isActive", connection.getIsActive());
                    connInfo.put("lastSync", connection.getLastSync());
                    connInfo.put("syncStatus", connection.getSyncStatus());
                    return connInfo;
                })
                .toList();
    }

    private List<Map<String, Object>> getRecentTransactions(User user) {
        // Временная заглушка - возвращаем последние 5 транзакций
        List<Transaction> transactions = transactionRepository.findByUserOrderByTimestampDesc(user);

        return transactions.stream()
                .limit(5)
                .map(tx -> {
                    Map<String, Object> txInfo = new HashMap<>();
                    txInfo.put("id", tx.getId());
                    txInfo.put("type", tx.getType());
                    txInfo.put("typeDisplayName", tx.getType().getDisplayName());
                    txInfo.put("baseAsset", tx.getBaseAsset());
                    txInfo.put("amount", tx.getAmount());
                    txInfo.put("price", tx.getPrice());
                    txInfo.put("timestamp", tx.getTimestamp());
                    return txInfo;
                })
                .toList();
    }

    private Map<String, Object> getTaxOverview(Long userId) {
        Map<String, Object> taxOverview = new HashMap<>();
        int currentYear = Year.now().getValue();

        try {
            TaxCalculationResultDto currentYearTax = taxCalculationService.calculateTaxes(userId, currentYear);
            TaxCalculationResultDto previousYearTax = taxCalculationService.calculateTaxes(userId, currentYear - 1);

            taxOverview.put("currentYear", currentYear);
            taxOverview.put("currentYearTax", currentYearTax.getTaxAmount());
            taxOverview.put("currentYearProfit", currentYearTax.getTaxableProfit());
            taxOverview.put("previousYearTax", previousYearTax.getTaxAmount());
            taxOverview.put("taxDeadline", LocalDateTime.of(currentYear + 1, 4, 30, 0, 0));

        } catch (Exception e) {
            // Если расчет не удался, возвращаем нулевые значения
            taxOverview.put("currentYear", currentYear);
            taxOverview.put("currentYearTax", BigDecimal.ZERO);
            taxOverview.put("currentYearProfit", BigDecimal.ZERO);
            taxOverview.put("previousYearTax", BigDecimal.ZERO);
            taxOverview.put("taxDeadline", LocalDateTime.of(currentYear + 1, 4, 30, 0, 0));
        }

        return taxOverview;
    }

    private List<Map<String, Object>> getQuickActions() {
        return List.of(
                Map.of(
                        "title", "Импорт транзакций",
                        "description", "Загрузите историю операций с бирж",
                        "action", "IMPORT_TRANSACTIONS",
                        "icon", "📥"
                ),
                Map.of(
                        "title", "Расчет налогов",
                        "description", "Рассчитайте налоги за выбранный период",
                        "action", "CALCULATE_TAXES",
                        "icon", "🧮"
                ),
                Map.of(
                        "title", "Подключить биржу",
                        "description", "Добавьте новое подключение к бирже",
                        "action", "CONNECT_EXCHANGE",
                        "icon", "🔗"
                ),
                Map.of(
                        "title", "Сгенерировать отчет",
                        "description", "Создайте налоговый отчет для ФНС",
                        "action", "GENERATE_REPORT",
                        "icon", "📊"
                )
        );
    }
}