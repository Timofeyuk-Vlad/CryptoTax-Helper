package com.cryptotax.helper.controller;

import com.cryptotax.helper.dto.ExchangeConnectionDto;
import com.cryptotax.helper.entity.ExchangeConnection;
import com.cryptotax.helper.service.ExchangeConnectionService;
import com.cryptotax.helper.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExchangeConnectionController {

    private final ExchangeConnectionService exchangeConnectionService;
    private final SecurityUtils securityUtils;

    @PostMapping("/connect")
    public ResponseEntity<?> createConnection(@Valid @RequestBody ExchangeConnectionDto connectionDto) {
        try {
            // TODO: Получить userId из JWT токена
            Long userId = securityUtils.getCurrentUserId(); // Временная заглушка

            ExchangeConnection connection = exchangeConnectionService.createConnection(userId, connectionDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Подключение к бирже успешно создано");
            response.put("connectionId", connection.getId());
            response.put("exchange", connection.getExchange());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/connections")
    public ResponseEntity<?> getUserConnections() {
        try {
            // TODO: Получить userId из JWT токена
            Long userId = securityUtils.getCurrentUserId(); // Временная заглушка

            List<ExchangeConnection> connections = exchangeConnectionService.getUserConnections(userId);

            return ResponseEntity.ok(connections);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/connections/{connectionId}")
    public ResponseEntity<?> deleteConnection(@PathVariable Long connectionId) {
        try {
            // TODO: Получить userId из JWT токена
            Long userId = securityUtils.getCurrentUserId(); // Временная заглушка

            exchangeConnectionService.deleteConnection(userId, connectionId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Подключение успешно удалено");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}