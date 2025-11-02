package com.cryptotax.helper.dto;

import com.cryptotax.helper.entity.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private TransactionType type;
    private String exchangeTxId;
    private String baseAsset;
    private String quoteAsset;
    private BigDecimal amount;
    private BigDecimal price;
    private BigDecimal total;
    private BigDecimal fee;
    private String feeAsset;
    private LocalDateTime timestamp;
    private String notes;
}