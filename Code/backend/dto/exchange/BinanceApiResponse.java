package com.cryptotax.helper.dto.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BinanceApiResponse<T> {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("msg")
    private String message;

    private List<T> data;
}