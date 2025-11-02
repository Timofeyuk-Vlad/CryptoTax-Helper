package com.cryptotax.helper.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TwoFactorAuthDto {

    @NotNull(message = "Код верификации обязателен")
    private Integer verificationCode;
}