package com.cryptotax.helper.service;

import com.cryptotax.helper.entity.SubscriptionPlan;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.exception.SubscriptionLimitException;
import com.cryptotax.helper.repository.ExchangeConnectionRepository;
import com.cryptotax.helper.repository.TransactionRepository;
import com.cryptotax.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserRepository userRepository;
    private final ExchangeConnectionRepository exchangeConnectionRepository;
    private final TransactionRepository transactionRepository;

    public boolean canAddExchangeConnection(Long userId) {
        User user = getUser(userId);
        long currentConnections = exchangeConnectionRepository.countByUserAndIsActiveTrue(user);
        return currentConnections < user.getMaxExchangeConnections();
    }

    public boolean canImportTransactions(Long userId) {
        User user = getUser(userId);
        long currentYearTransactions = getCurrentYearTransactionCount(user);
        return currentYearTransactions < user.getMaxTransactionsPerYear();
    }

    public boolean canGenerateTaxReports(Long userId) {
        User user = getUser(userId);
        return !SubscriptionPlan.FREE.name().equals(user.getSubscriptionType());
    }

    public void checkExchangeConnectionLimit(Long userId) {
        if (!canAddExchangeConnection(userId)) {
            throw new SubscriptionLimitException(
                    "Превышен лимит подключений к биржам для вашего тарифа. " +
                            "Улучшите подписку для добавления новых подключений."
            );
        }
    }

    public void checkTransactionImportLimit(Long userId) {
        if (!canImportTransactions(userId)) {
            throw new SubscriptionLimitException(
                    "Превышен лимит транзакций для вашего тарифа. " +
                            "Улучшите подписку для импорта дополнительных транзакций."
            );
        }
    }

    public void checkTaxReportGeneration(Long userId) {
      //  if (!canGenerateTaxReports(userId)) {
      //      throw new SubscriptionLimitException(
      //              "Генерация налоговых отчетов недоступна на бесплатном тарифе. " +
      //                      "Улучшите подписку для доступа к этой функции."
      //      );
      //  }
    }

    public boolean isSubscriptionActive(User user) {
        if (user.getSubscriptionExpires() == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(user.getSubscriptionExpires());
    }

    public SubscriptionPlan getUserSubscriptionPlan(Long userId) {
        User user = getUser(userId);
        return SubscriptionPlan.fromString(user.getSubscriptionType());
    }

    public void validateSubscriptionAccess(Long userId) {
        User user = getUser(userId);
        if (!isSubscriptionActive(user) && !SubscriptionPlan.FREE.name().equals(user.getSubscriptionType())) {
            throw new SubscriptionLimitException(
                    "Ваша подписка истекла. Пожалуйста, продлите подписку для доступа к премиум функциям."
            );
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    private long getCurrentYearTransactionCount(User user) {
        int currentYear = Year.now().getValue();
        return transactionRepository.findByUserAndYearOrderByTimestampDesc(user, currentYear).size();
    }

    public void applySubscriptionLimits(User user, SubscriptionPlan plan) {
        user.setMaxExchangeConnections(plan.getMaxExchangeConnections());
        user.setMaxTransactionsPerYear(plan.getMaxTransactionsPerYear());

        // Обновляем роли в зависимости от подписки
        if (plan != SubscriptionPlan.FREE) {
            user.addRole(com.cryptotax.helper.entity.UserRole.ROLE_PREMIUM);
        } else {
            user.removeRole(com.cryptotax.helper.entity.UserRole.ROLE_PREMIUM);
        }
    }
}