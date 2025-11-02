package com.example.lowflightzone.controllers;

import com.example.lowflightzone.dto.FlightDto;
import com.example.lowflightzone.services.ExternalFlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/external")
public class ExternalFlightController {

    private final ExternalFlightService externalFlightService;

    @Autowired
    public ExternalFlightController(ExternalFlightService externalFlightService) {
        this.externalFlightService = externalFlightService;
    }

    @GetMapping("/flight/{flightNumber}")
    public FlightDto getExternalFlight(@PathVariable String flightNumber) {
        System.out.println("🚀 Контроллер вызван с рейсом: " + flightNumber);
        return externalFlightService.fetchFlightByNumber(flightNumber);
    }

}
