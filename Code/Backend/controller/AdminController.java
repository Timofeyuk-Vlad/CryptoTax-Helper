package com.cryptotax.helper.controller;

import com.cryptotax.helper.entity.User;
import com.cryptotax.helper.entity.UserRole;
import com.cryptotax.helper.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            // TODO: Проверка прав администратора
            List<User> users = userService.getAllUsers();

            // Не возвращаем пароли в ответе
            List<Map<String, Object>> userResponses = users.stream()
                    .map(user -> {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("id", user.getId());
                        userData.put("email", user.getEmail());
                        userData.put("firstName", user.getFirstName());
                        userData.put("lastName", user.getLastName());
                        userData.put("roles", user.getRoles());
                        userData.put("isEnabled", user.getIsEnabled());
                        userData.put("subscriptionType", user.getSubscriptionType());
                        userData.put("createdAt", user.getCreatedAt());
                        userData.put("lastLogin", user.getLastLogin());
                        return userData;
                    })
                    .toList();

            return ResponseEntity.ok(userResponses);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении списка пользователей: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            // TODO: Проверка прав администратора
            User user = userService.getUserById(userId);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("roles", user.getRoles());
            userData.put("isEnabled", user.getIsEnabled());
            userData.put("is2faEnabled", user.getIs2faEnabled());
            userData.put("subscriptionType", user.getSubscriptionType());
            userData.put("subscriptionExpires", user.getSubscriptionExpires());
            userData.put("maxExchangeConnections", user.getMaxExchangeConnections());
            userData.put("maxTransactionsPerYear", user.getMaxTransactionsPerYear());
            userData.put("createdAt", user.getCreatedAt());
            userData.put("lastLogin", user.getLastLogin());

            return ResponseEntity.ok(userData);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении пользователя: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<?> addRoleToUser(@PathVariable Long userId, @RequestParam UserRole role) {
        try {
            // TODO: Проверка прав администратора
            User user = userService.addRoleToUser(userId, role);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Роль " + role.getDisplayName() + " добавлена пользователю");
            response.put("userId", user.getId());
            response.put("roles", user.getRoles());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при добавлении роли: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/users/{userId}/roles")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable Long userId, @RequestParam UserRole role) {
        try {
            // TODO: Проверка прав администратора
            User user = userService.removeRoleFromUser(userId, role);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Роль " + role.getDisplayName() + " удалена у пользователя");
            response.put("userId", user.getId());
            response.put("roles", user.getRoles());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при удалении роли: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/users/{userId}/subscription")
    public ResponseEntity<?> updateUserSubscription(
            @PathVariable Long userId,
            @RequestParam String subscriptionType,
            @RequestParam(defaultValue = "1") Integer months) {
        try {
            // TODO: Проверка прав администратора
            User user = userService.updateUserSubscription(userId, subscriptionType, months);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Подписка пользователя обновлена на " + subscriptionType);
            response.put("userId", user.getId());
            response.put("subscriptionType", user.getSubscriptionType());
            response.put("subscriptionExpires", user.getSubscriptionExpires());
            response.put("maxExchangeConnections", user.getMaxExchangeConnections());
            response.put("maxTransactionsPerYear", user.getMaxTransactionsPerYear());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при обновлении подписки: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/users/{userId}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long userId) {
        try {
            // TODO: Проверка прав администратора
            User user = userService.getUserById(userId);
            user.setIsEnabled(true);
            userService.updateUserSubscription(userId, user.getSubscriptionType(), null); // Сохраняем изменения

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Пользователь активирован");
            response.put("userId", user.getId());
            response.put("isEnabled", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при активации пользователя: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/users/{userId}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long userId) {
        try {
            // TODO: Проверка прав администратора
            User user = userService.getUserById(userId);
            user.setIsEnabled(false);
            userService.updateUserSubscription(userId, user.getSubscriptionType(), null); // Сохраняем изменения

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Пользователь деактивирован");
            response.put("userId", user.getId());
            response.put("isEnabled", false);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при деактивации пользователя: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        try {
            // TODO: Проверка прав администратора
            List<User> users = userService.getAllUsers();

            long totalUsers = users.size();
            long activeUsers = users.stream().filter(User::getIsEnabled).count();
            long premiumUsers = users.stream().filter(User::isPremium).count();
            long usersWith2FA = users.stream().filter(user -> user.getIs2faEnabled() != null && user.getIs2faEnabled()).count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("premiumUsers", premiumUsers);
            stats.put("usersWith2FA", usersWith2FA);
            stats.put("freeUsers", totalUsers - premiumUsers);
            stats.put("systemTime", java.time.LocalDateTime.now());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении статистики: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}