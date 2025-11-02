package com.example.lowflightzone.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionRequest {
    private Integer flightId;
    private String flightNumber;
    private String endpoint;
    private String p256dh;
    private String auth;
}
