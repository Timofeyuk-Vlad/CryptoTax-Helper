package com.cryptotax.helper.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    private String password;

    @NotBlank
    private String confirmPassword;

    private String firstName;

    private String lastName;
}