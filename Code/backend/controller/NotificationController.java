package com.cryptotax.helper.controller;

import com.cryptotax.helper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getUserNotifications() {
        try {
            Long userId = 1L; // Временная заглушка

            var notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении уведомлений: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        try {
            Long userId = 1L; // Временная заглушка

            var notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении непрочитанных уведомлений: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            Long userId = 1L; // Временная заглушка

            long count = notificationService.getUnreadCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", count);
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении количества уведомлений: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        try {
            Long userId = 1L; // Временная заглушка

            notificationService.markAsRead(notificationId, userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Уведомление помечено как прочитанное");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при отметке уведомления: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        try {
            Long userId = 1L; // Временная заглушка

            notificationService.markAllAsRead(userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Все уведомления помечены как прочитанные");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при отметке всех уведомлений: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/test")
    public ResponseEntity<?> createTestNotification() {
        try {
            Long userId = 1L; // Временная заглушка

            var notification = notificationService.createNotification(
                    userId,
                    com.cryptotax.helper.entity.NotificationType.SYSTEM_ANNOUNCEMENT,
                    "Тестовое уведомление",
                    "Это тестовое уведомление для проверки работы системы",
                    true,
                    "/dashboard",
                    "Перейти в дашборд"
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Тестовое уведомление создано");
            response.put("notificationId", notification.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при создании тестового уведомления: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}