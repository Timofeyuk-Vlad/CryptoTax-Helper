package com.example.lowflightzone.controllers;

import com.example.lowflightzone.dto.AirportDto;
import com.example.lowflightzone.services.AirportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/airports")
@Tag(name = "Airport Controller", description = "API для управления аэропортами")
public class AirportController {

    private final AirportService airportService;

    @Autowired
    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @Operation(summary = "Получить все аэропорты")
    @GetMapping
    public ResponseEntity<List<AirportDto>> getAllAirports() {
        List<AirportDto> airports = airportService.getAllAirports();
        return ResponseEntity.ok(airports);
    }

    @Operation(summary = "Получить аэропорт по коду IATA")
    @GetMapping("/{iataCode}")
    public ResponseEntity<AirportDto> getAirportByCode(@PathVariable String iataCode) {
        AirportDto airport = airportService.getAirportByCode(iataCode);
        return ResponseEntity.ok(airport);
    }

    @Operation(summary = "Найти аэропорты по городу")
    @GetMapping("/city/{city}")
    public ResponseEntity<List<AirportDto>> getAirportsByCity(@PathVariable String city) {
        List<AirportDto> airports = airportService.getAirportsByCity(city);
        return ResponseEntity.ok(airports);
    }

    @Operation(summary = "Добавить новый аэропорт")
    @PostMapping
    public ResponseEntity<AirportDto> addAirport(@RequestBody AirportDto airportDto) {
        AirportDto newAirport = airportService.addAirport(airportDto);
        return ResponseEntity.ok(newAirport);
    }
}