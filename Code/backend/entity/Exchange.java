package com.cryptotax.helper.entity;

public enum Exchange {
    BINANCE("Binance"),
    BYBIT("ByBit"),
    OKX("OKX"),
    HUOBI("Huobi"),
    KUCOIN("KuCoin"),
    GATEIO("Gate.io"),
    MEXC("MEXC");

    private final String displayName;

    Exchange(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}