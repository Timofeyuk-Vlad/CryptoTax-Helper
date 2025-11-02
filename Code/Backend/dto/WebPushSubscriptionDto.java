package com.example.lowflightzone.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebPushSubscriptionDto {
    private String endpoint;
    private String p256dh;
    private String auth;
    // getters/setters
}
