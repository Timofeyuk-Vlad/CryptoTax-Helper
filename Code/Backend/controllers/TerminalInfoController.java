package com.example.lowflightzone.controllers;

import com.example.lowflightzone.dto.TerminalInfoDto;
import com.example.lowflightzone.services.AirportTerminalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/terminals")
@Tag(name = "Terminal Controller", description = "API для информации о терминалах аэропортов")
public class TerminalInfoController {

    private final AirportTerminalService terminalService;

    @Autowired
    public TerminalInfoController(AirportTerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @Operation(summary = "Получить информацию о терминале")
    @GetMapping("/{airportCode}/{terminal}")
    public ResponseEntity<TerminalInfoDto> getTerminalInfo(
            @PathVariable String airportCode,
            @PathVariable String terminal) {

        TerminalInfoDto info = terminalService.getTerminalInfo(airportCode, terminal);
        return ResponseEntity.ok(info);
    }

    @Operation(summary = "Получить все терминалы аэропорта")
    @GetMapping("/airport/{airportCode}")
    public ResponseEntity<List<TerminalInfoDto>> getAirportTerminals(
            @PathVariable String airportCode) {

        List<TerminalInfoDto> terminals = terminalService.getAirportTerminals(airportCode);
        return ResponseEntity.ok(terminals);
    }

    @Operation(summary = "Обновить информацию о терминале")
    @PutMapping("/{airportCode}/{terminal}")
    public ResponseEntity<TerminalInfoDto> updateTerminalInfo(
            @PathVariable String airportCode,
            @PathVariable String terminal,
            @RequestBody TerminalInfoDto infoDto) {

        TerminalInfoDto updatedInfo = terminalService.updateTerminalInfo(airportCode, terminal, infoDto);
        return ResponseEntity.ok(updatedInfo);
    }

    @Operation(summary = "Получить терминалы с высокой загруженностью")
    @GetMapping("/congestion/high")
    public ResponseEntity<List<TerminalInfoDto>> getHighCongestionTerminals() {
        List<TerminalInfoDto> terminals = terminalService.getHighCongestionTerminals();
        return ResponseEntity.ok(terminals);
    }
}