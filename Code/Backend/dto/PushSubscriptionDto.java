// com.example.lowflightzone.dto.PushSubscriptionDto
package com.example.lowflightzone.dto;
import lombok.Data;

@Data
public class PushSubscriptionDto {
    private String endpoint;
    private String p256dh;
    private String auth;
}
