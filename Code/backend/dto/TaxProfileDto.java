package com.cryptotax.helper.dto;

import com.cryptotax.helper.entity.Country;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaxProfileDto {

    @NotNull
    private Country country;

    private String taxIdentificationNumber;

    private BigDecimal expectedAnnualIncome;

    private Boolean applyTaxFreeAllowance = true;

    private String bankAccountNumber;
}