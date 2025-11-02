package com.example.lowflightzone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class FlightViewHistoryDto {
    private Integer id;
    private UserDto user;
    private FlightDto flight;
    private LocalDateTime viewedAt;
    private Integer viewCount;
}
