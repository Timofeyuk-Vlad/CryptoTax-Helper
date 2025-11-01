package com.cryptotax.helper.dto.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BinanceAccountInfo {
    @JsonProperty("canTrade")
    private Boolean canTrade;

    @JsonProperty("canWithdraw")
    private Boolean canWithdraw;

    @JsonProperty("canDeposit")
    private Boolean canDeposit;

    @JsonProperty("accountType")
    private String accountType;
}