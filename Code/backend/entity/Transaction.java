package com.cryptotax.helper.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_connection_id")
    private ExchangeConnection exchangeConnection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "exchange_tx_id", length = 100)
    private String exchangeTxId;

    @Column(name = "base_asset", nullable = false, length = 20)
    private String baseAsset;

    @Column(name = "quote_asset", length = 20)
    private String quoteAsset;

    @Column(name = "amount", precision = 18, scale = 8)
    private BigDecimal amount;

    @Column(name = "price", precision = 18, scale = 8)
    private BigDecimal price;

    @Column(name = "total", precision = 18, scale = 8)
    private BigDecimal total;

    @Column(name = "fee", precision = 18, scale = 8)
    private BigDecimal fee;

    @Column(name = "fee_asset", length = 20)
    private String feeAsset;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;

    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed = false;

    @Column(name = "notes", length = 500)
    private String notes;

    public Transaction(User user, TransactionType type, String baseAsset, LocalDateTime timestamp) {
        this.user = user;
        this.type = type;
        this.baseAsset = baseAsset;
        this.timestamp = timestamp;
        this.importedAt = LocalDateTime.now();
    }
}