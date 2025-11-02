package com.cryptotax.helper.dto.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BinanceTradeDto {
    @JsonProperty("id")
    private Long tradeId;

    @JsonProperty("orderId")
    private Long orderId;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("price")
    private String price;

    @JsonProperty("qty")
    private String quantity;

    @JsonProperty("quoteQty")
    private String quoteQuantity;

    @JsonProperty("commission")
    private String commission;

    @JsonProperty("commissionAsset")
    private String commissionAsset;

    @JsonProperty("time")
    private Long timestamp;

    @JsonProperty("isBuyer")
    private Boolean isBuyer;

    @JsonProperty("isMaker")
    private Boolean isMaker;

    @JsonProperty("isBestMatch")
    private Boolean isBestMatch;
}