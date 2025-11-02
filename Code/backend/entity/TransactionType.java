package com.cryptotax.helper.entity;

public enum TransactionType {
    BUY("Покупка"),
    SELL("Продажа"),
    SWAP("Обмен"),
    DEPOSIT("Депозит"),
    WITHDRAWAL("Вывод"),
    MINING("Майнинг"),
    STAKING("Стейкинг"),
    REWARD("Награда"),
    COMMISSION("Комиссия"),
    P2P("P2P операция");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}