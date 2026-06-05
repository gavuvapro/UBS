package com.utilitybilling.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TaxConfig entity defining applicable tax rates (e.g., VAT).
 * The active tax rate is applied to the consumption charge + service charge during bill generation.
 */
@Entity
@Table(name = "tax_configs")
@EntityListeners(AuditingEntityListener.class)
public class TaxConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String taxName;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaxStatus status = TaxStatus.ACTIVE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum TaxStatus {
        ACTIVE, INACTIVE
    }

    public TaxConfig() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTaxName() { return taxName; }
    public void setTaxName(String taxName) { this.taxName = taxName; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public TaxStatus getStatus() { return status; }
    public void setStatus(TaxStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
