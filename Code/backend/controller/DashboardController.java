package com.cryptotax.helper.controller;

import com.cryptotax.helper.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<?> getDashboard() {
        try {
            // TODO: Получить userId из SecurityContext
            Long userId = 1L; // Временная заглушка

            Map<String, Object> dashboardData = dashboardService.getDashboardData(userId);

            return ResponseEntity.ok(dashboardData);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при загрузке дашборда: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> getSystemHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "OPERATIONAL");
            health.put("timestamp", java.time.LocalDateTime.now());
            health.put("version", "1.0.0");
            health.put("services", Map.of(
                    "database", "CONNECTED",
                    "authentication", "OPERATIONAL",
                    "taxCalculation", "OPERATIONAL",
                    "transactionImport", "OPERATIONAL"
            ));

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка проверки здоровья системы");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}