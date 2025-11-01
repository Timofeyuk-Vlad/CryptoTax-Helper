package com.cryptotax.helper.dto;

import com.cryptotax.helper.entity.Exchange;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeConnectionDto {

    @NotNull(message = "Биржа обязательна")
    private Exchange exchange;

    private String apiKey;

    private String apiSecret;

    private Boolean isActive = true;
}