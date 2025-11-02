package com.example.lowflightzone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class AirportDto {
    private String iataCode;
    private String name;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private Integer altitude;
}
