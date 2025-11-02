package com.example.lowflightzone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class TerminalInfoDto {
    private Integer id;
    private AirportDto airport;
    private String terminal;
    private String congestionLevel;
    private Integer waitTimeMinutes;
    private String weatherConditions;
    private LocalDateTime lastUpdated;
}