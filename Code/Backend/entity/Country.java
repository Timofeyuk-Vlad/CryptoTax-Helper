package com.cryptotax.helper.entity;

public enum Country {
    RUSSIA("Россия", "RUB", "₽"),
    BELARUS("Беларусь", "BYN", "Br");

    private final String displayName;
    private final String currency;
    private final String currencySymbol;

    Country(String displayName, String currency, String currencySymbol) {
        this.displayName = displayName;
        this.currency = currency;
        this.currencySymbol = currencySymbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }
}