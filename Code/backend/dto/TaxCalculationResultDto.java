package com.cryptotax.helper.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaxCalculationResultDto {
    private int taxYear;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal taxableProfit;
    private BigDecimal taxAmount;
    private int transactionCount;
    private String currency = "RUB"; // По умолчанию рубли

    public TaxCalculationResultDto() {
        this.totalIncome = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
        this.taxableProfit = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
    }
}