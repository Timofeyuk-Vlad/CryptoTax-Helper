package com.example.lowflightzone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class FlightSubscriptionDto {
    private Integer id;
    private FlightDto flight;
    private UserDto user; // уже есть
    private String notificationTypes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastNotifiedAt;
    private Integer notifyBeforeHours;
    private Integer minDelayMinutes;
}