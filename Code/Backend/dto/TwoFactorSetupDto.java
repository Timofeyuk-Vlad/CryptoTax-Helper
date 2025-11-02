package com.cryptotax.helper.dto;

import lombok.Data;

@Data
public class TwoFactorSetupDto {
    private String secret;
    private String qrCodeUrl;
    private String message;
}