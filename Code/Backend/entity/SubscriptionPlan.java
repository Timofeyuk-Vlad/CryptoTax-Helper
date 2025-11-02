package com.cryptotax.helper.entity;

import lombok.Getter;

@Getter
public enum SubscriptionPlan {
    FREE("Бесплатный", 0, 3, 1000, false, "Базовые функции"),
    PREMIUM("Премиум", 990, 10, 10000, true, "Расширенные возможности"),
    PRO("Профессиональный", 2990, Integer.MAX_VALUE, Integer.MAX_VALUE, true, "Полный функционал");

    private final String displayName;
    private final int monthlyPrice; // в рублях
    private final int maxExchangeConnections;
    private final int maxTransactionsPerYear;
    private final boolean taxReportGeneration;
    private final String description;

    SubscriptionPlan(String displayName, int monthlyPrice, int maxExchangeConnections,
                     int maxTransactionsPerYear, boolean taxReportGeneration, String description) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.maxExchangeConnections = maxExchangeConnections;
        this.maxTransactionsPerYear = maxTransactionsPerYear;
        this.taxReportGeneration = taxReportGeneration;
        this.description = description;
    }

    public static SubscriptionPlan fromString(String plan) {
        for (SubscriptionPlan subscriptionPlan : values()) {
            if (subscriptionPlan.name().equalsIgnoreCase(plan)) {
                return subscriptionPlan;
            }
        }
        return FREE; // по умолчанию
    }
}