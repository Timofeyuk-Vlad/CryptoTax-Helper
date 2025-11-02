package com.cryptotax.helper.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tax_profiles")
public class TaxProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Country country;

    @Column(name = "tax_identification_number", length = 50)
    private String taxIdentificationNumber;

    @Column(name = "expected_annual_income", precision = 15, scale = 2)
    private BigDecimal expectedAnnualIncome;

    @Column(name = "apply_tax_free_allowance", nullable = false)
    private Boolean applyTaxFreeAllowance = true;

    @Column(name = "bank_account_number", length = 100)
    private String bankAccountNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "tax_year", length = 4)
    private String taxYear;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TaxProfile(User user, Country country) {
        this.user = user;
        this.country = country;
        this.isActive = true;
    }
}