package com.cryptotax.helper.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class TaxCalculationResultDto {
    private int taxYear;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal taxableProfit;
    private BigDecimal taxAmount;
    private int transactionCount;
    private String currency = "RUB";
    private String country = "RUSSIA";
    private String calculationMethod = "FIFO";
    private Map<String, Object> fifoDetails; // Детали FIFO расчета
    private boolean demoMode = true;

    public TaxCalculationResultDto() {
        this.totalIncome = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
        this.taxableProfit = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
    }
}