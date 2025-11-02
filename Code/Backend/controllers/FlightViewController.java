package com.example.lowflightzone.controllers;

import com.example.lowflightzone.dto.FlightViewHistoryDto;
import com.example.lowflightzone.services.FlightViewHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/flight-views")
@RequiredArgsConstructor
@Tag(name = "Flight View History", description = "API для управления историей просмотров рейсов")
public class FlightViewController {

    private final FlightViewHistoryService viewHistoryService;

    @Operation(summary = "Записать просмотр рейса для текущего пользователя")
    @PostMapping("/record/{flightId}")
    public ResponseEntity<FlightViewHistoryDto> recordFlightView(@PathVariable Integer flightId) {
        FlightViewHistoryDto viewHistory = viewHistoryService.recordFlightView(flightId);
        return ResponseEntity.ok(viewHistory);
    }

    @Operation(summary = "Записать просмотр рейса для конкретного пользователя (только для админов)")
    @PostMapping("/admin/record")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlightViewHistoryDto> recordFlightViewForUser(
            @RequestParam Integer userId,
            @RequestParam Integer flightId) {

        FlightViewHistoryDto viewHistory = viewHistoryService.recordFlightView(userId, flightId);
        return ResponseEntity.ok(viewHistory);
    }

    @Operation(summary = "Получить последние просмотренные рейсы текущего пользователя")
    @GetMapping("/my/recent")
    public ResponseEntity<List<FlightViewHistoryDto>> getMyRecentViews(
            @RequestParam(defaultValue = "10") int limit) {

        List<FlightViewHistoryDto> recentViews = viewHistoryService.getCurrentUserRecentViews(limit);
        return ResponseEntity.ok(recentViews);
    }

    @Operation(summary = "Получить последние просмотренные рейсы пользователя (только для админов)")
    @GetMapping("/admin/user/{userId}/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FlightViewHistoryDto>> getUserRecentViews(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<FlightViewHistoryDto> recentViews = viewHistoryService.getRecentViews(userId, limit);
        return ResponseEntity.ok(recentViews);
    }

    @Operation(summary = "Получить историю просмотров текущего пользователя")
    @GetMapping("/my")
    public ResponseEntity<List<FlightViewHistoryDto>> getMyViewHistory() {
        List<FlightViewHistoryDto> history = viewHistoryService.getCurrentUserViewHistory();
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Очистить историю просмотров текущего пользователя")
    @DeleteMapping("/my")
    public ResponseEntity<String> clearMyHistory() {
        viewHistoryService.clearCurrentUserHistory();
        return ResponseEntity.ok("Your view history has been cleared successfully");
    }

    // Остальные методы аналогично...
}