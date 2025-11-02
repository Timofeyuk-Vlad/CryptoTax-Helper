package com.example.lowflightzone.controllers;

import com.example.lowflightzone.dao.FlightDao;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final FlightDao flightDao;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "OK",
                "service", "LowFlightZone API",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
    }

    @GetMapping("/db-status")
    public Map<String, Object> dbStatus() {
        long flightCount = flightDao.findAll().size();
        return Map.of(
                "database", "connected",
                "flights_count", flightCount,
                "status", flightCount > 0 ? "READY" : "EMPTY"
        );
    }
}