package com.cryptotax.helper.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SubscriptionLimitException.class)
    public ResponseEntity<?> handleSubscriptionLimit(SubscriptionLimitException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "SUBSCRIPTION_LIMIT_EXCEEDED");
        response.put("message", e.getMessage());
        response.put("upgradeRequired", true);
        response.put("code", "LIMIT_EXCEEDED");
        return ResponseEntity.status(402).body(response); // 402 Payment Required
    }
}