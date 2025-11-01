package com.cryptotax.helper.dto;

import lombok.Data;

@Data
public class JwtResponseDto {
    private String token;
    private String type = "Bearer";
    private String email;
    private String message;

    public JwtResponseDto(String token, String email, String message) {
        this.token = token;
        this.email = email;
        this.message = message;
    }
}