package com.example.lowflightzone.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
    private String firstName;   // добавлено
    private String lastName;    // добавлено
    private String phoneNumber; // добавлено
}
