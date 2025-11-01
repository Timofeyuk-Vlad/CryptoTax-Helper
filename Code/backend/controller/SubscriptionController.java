package com.cryptotax.helper.controller;

import com.cryptotax.helper.entity.SubscriptionPlan;
import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.repository.ExchangeConnectionRepository;
import com.cryptotax.helper.repository.TransactionRepository;
import com.cryptotax.helper.repository.UserRepository;
import com.cryptotax.helper.service.SubscriptionService;
import com.cryptotax.helper.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ExchangeConnectionRepository exchangeConnectionRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping("/plans")
    public ResponseEntity<List<Map<String, Object>>> getAvailablePlans() {
        List<Map<String, Object>> plans = Arrays.stream(SubscriptionPlan.values())
                .map(plan -> {
                    Map<String, Object> planInfo = new HashMap<>();
                    planInfo.put("name", plan.name());
                    planInfo.put("displayName", plan.getDisplayName());
                    planInfo.put("monthlyPrice", plan.getMonthlyPrice());
                    planInfo.put("maxExchangeConnections", plan.getMaxExchangeConnections());
                    planInfo.put("maxTransactionsPerYear", plan.getMaxTransactionsPerYear());
                    planInfo.put("taxReportGeneration", plan.isTaxReportGeneration());
                    planInfo.put("description", plan.getDescription());
                    return planInfo;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(plans);
    }

    @GetMapping("/limits")
    public ResponseEntity<?> getCurrentLimits() {
        try {
            // Временно используем первого пользователя из базы для тестирования
            User user = userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Пользователи не найдены"));

            return getLimitsForUser(user);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/limits/{userId}")
    public ResponseEntity<?> getLimitsForUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            return getLimitsForUser(user);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/upgrade")
    public ResponseEntity<?> upgradeSubscription(
            @RequestParam String plan,
            @RequestParam Integer months) {

        try {
            // Временно используем первого пользователя для тестирования
            User user = userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Пользователи не найдены"));

            return upgradeUserSubscription(user.getId(), plan, months);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/upgrade/{userId}")
    public ResponseEntity<?> upgradeUserSubscription(
            @PathVariable Long userId,
            @RequestParam String plan,
            @RequestParam Integer months) {

        try {
            User updatedUser = userService.updateUserSubscription(userId, plan, months);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Тариф успешно обновлен");
            response.put("userId", updatedUser.getId());
            response.put("newPlan", updatedUser.getSubscriptionType());
            response.put("expires", updatedUser.getSubscriptionExpires());
            response.put("maxExchangeConnections", updatedUser.getMaxExchangeConnections());
            response.put("maxTransactionsPerYear", updatedUser.getMaxTransactionsPerYear());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSubscription() {
        try {
            // Временно используем первого пользователя для тестирования
            User user = userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Пользователи не найдены"));

            return getSubscriptionForUser(user);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/current/{userId}")
    public ResponseEntity<?> getSubscriptionForUser(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            return getSubscriptionForUser(user);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/check-access")
    public ResponseEntity<?> checkFeatureAccess(@RequestParam String feature) {
        try {
            // Временно используем первого пользователя для тестирования
            User user = userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Пользователи не найдены"));

            return checkFeatureAccessForUser(user.getId(), feature);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/check-access/{userId}")
    public ResponseEntity<?> checkFeatureAccessForUser(
            @PathVariable Long userId,
            @RequestParam String feature) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean hasAccess = false;
            String message = "";

            switch (feature.toUpperCase()) {
                case "EXCHANGE_CONNECTION":
                    hasAccess = subscriptionService.canAddExchangeConnection(user.getId());
                    message = hasAccess ? "Доступ разрешен" : "Превышен лимит подключений";
                    break;
                case "TRANSACTION_IMPORT":
                    hasAccess = subscriptionService.canImportTransactions(user.getId());
                    message = hasAccess ? "Доступ разрешен" : "Превышен лимит транзакций";
                    break;
                case "TAX_REPORT":
                    hasAccess = subscriptionService.canGenerateTaxReports(user.getId());
                    message = hasAccess ? "Доступ разрешен" : "Функция недоступна на текущем тарифе";
                    break;
                default:
                    message = "Неизвестная функция";
            }

            Map<String, Object> response = new HashMap<>();
            response.put("feature", feature);
            response.put("hasAccess", hasAccess);
            response.put("message", message);
            response.put("userId", user.getId());
            response.put("subscriptionType", user.getSubscriptionType());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/test-all")
    public ResponseEntity<?> testAllFeatures() {
        try {
            // Временно используем первого пользователя для тестирования
            User user = userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Пользователи не найдены"));

            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getId());
            result.put("userEmail", user.getEmail());
            result.put("subscriptionType", user.getSubscriptionType());

            // Проверяем все функции
            result.put("canAddExchangeConnection", subscriptionService.canAddExchangeConnection(user.getId()));
            result.put("canImportTransactions", subscriptionService.canImportTransactions(user.getId()));
            result.put("canGenerateTaxReports", subscriptionService.canGenerateTaxReports(user.getId()));

            // Текущие лимиты
            result.put("currentExchangeConnections", exchangeConnectionRepository.countByUserAndIsActiveTrue(user));
            result.put("currentYearTransactions", transactionRepository.findByUserAndYearOrderByTimestampDesc(
                    user, Year.now().getValue()).size());
            result.put("maxExchangeConnections", user.getMaxExchangeConnections());
            result.put("maxTransactionsPerYear", user.getMaxTransactionsPerYear());

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Вспомогательные методы
    private ResponseEntity<?> getLimitsForUser(User user) {
        long currentConnections = exchangeConnectionRepository.countByUserAndIsActiveTrue(user);
        long currentYearTransactions = transactionRepository.findByUserAndYearOrderByTimestampDesc(
                user, Year.now().getValue()).size();

        Map<String, Object> limits = new HashMap<>();
        limits.put("userId", user.getId());
        limits.put("userEmail", user.getEmail());
        limits.put("subscriptionType", user.getSubscriptionType());
        limits.put("maxExchangeConnections", user.getMaxExchangeConnections());
        limits.put("maxTransactionsPerYear", user.getMaxTransactionsPerYear());
        limits.put("currentExchangeConnections", currentConnections);
        limits.put("currentYearTransactions", currentYearTransactions);
        limits.put("canGenerateReports", subscriptionService.canGenerateTaxReports(user.getId()));
        limits.put("canAddExchangeConnection", subscriptionService.canAddExchangeConnection(user.getId()));
        limits.put("canImportTransactions", subscriptionService.canImportTransactions(user.getId()));

        return ResponseEntity.ok(limits);
    }

    private ResponseEntity<?> getSubscriptionForUser(User user) {
        SubscriptionPlan currentPlan = subscriptionService.getUserSubscriptionPlan(user.getId());
        boolean isActive = subscriptionService.isSubscriptionActive(user);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("userEmail", user.getEmail());
        response.put("plan", currentPlan.name());
        response.put("displayName", currentPlan.getDisplayName());
        response.put("isActive", isActive);
        response.put("expires", user.getSubscriptionExpires());
        response.put("description", currentPlan.getDescription());

        return ResponseEntity.ok(response);
    }
}