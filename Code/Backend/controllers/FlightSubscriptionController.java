package com.example.lowflightzone.controllers;

import com.example.lowflightzone.dto.FlightSubscriptionDto;
import com.example.lowflightzone.dto.SubscriptionRequest;
import com.example.lowflightzone.services.FlightSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscription Controller", description = "API для управления подписками на рейсы")
public class FlightSubscriptionController {

    private final FlightSubscriptionService subscriptionService;

    @Operation(summary = "Подписаться на рейс и сохранить Web Push параметры")
    @PostMapping("/subscribe")
    public ResponseEntity<FlightSubscriptionDto> subscribeToFlight(@RequestBody SubscriptionRequest request) {

        FlightSubscriptionDto dto = subscriptionService.subscribeFlexible(
                request.getFlightId(),
                request.getFlightNumber(),
                request.getEndpoint(),
                request.getP256dh(),
                request.getAuth()
        );

        return ResponseEntity.ok(dto);
    }



    @Operation(summary = "Отписаться от рейса (можно передавать subscriptionId или flightId/flightNumber)")
    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribeFromFlight(
            @RequestParam(required = false) Integer subscriptionId,
            @RequestParam(required = false) Integer flightId,
            @RequestParam(required = false) String flightNumber
    ) {
        if (subscriptionId == null && flightId == null && (flightNumber == null || flightNumber.isBlank())) {
            return ResponseEntity.badRequest().body("Нужно передать subscriptionId или flightId/flightNumber");
        }
        subscriptionService.unsubscribeFlexible(subscriptionId, flightId, flightNumber);
        return ResponseEntity.ok("Подписка успешно отменена");
    }

    @Operation(summary = "Получить подписки пользователя")
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<FlightSubscriptionDto>> getUserSubscriptions(
            @PathVariable String userEmail
    ) {
        return ResponseEntity.ok(subscriptionService.getUserSubscriptions(userEmail));
    }

    @Operation(summary = "Получить подписки на конкретный рейс")
    @GetMapping("/flight/{flightNumber}")
    public ResponseEntity<List<FlightSubscriptionDto>> getSubscriptionsForFlight(
            @PathVariable String flightNumber
    ) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsForFlight(flightNumber));
    }
}
