package com.example.lowflightzone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class FlightDto {
    private Integer id;
    private String flightNumber;
    private String airline;
    private AirportDto departureAirport;
    private AirportDto arrivalAirport;

    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;
    private LocalDateTime estimatedDeparture;
    private LocalDateTime estimatedArrival;
    private LocalDateTime actualDeparture;
    private LocalDateTime actualArrival;

    private Integer delayMinutes;
    private String terminal;
    private String gate;
    private String status;
    private LocalDateTime lastUpdated;
    private Integer viewCount;
    private LocalDateTime viewedAt;

    private int subscriptionCount;
    private boolean subscribed;
}
