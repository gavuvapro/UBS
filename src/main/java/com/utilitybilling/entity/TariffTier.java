package com.utilitybilling.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TariffTier entity representing a single pricing band within a tiered tariff.
 * Defines min/max units and price per unit for progressive billing.
 */
@Entity
@Table(name = "tariff_tiers")
@EntityListeners(AuditingEntityListener.class)
public class TariffTier {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tariff_id", referencedColumnName = "id", nullable = false)
    private Tariff tariff;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal minUnits;

    @Column(precision = 18, scale = 2)
    private BigDecimal maxUnits;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal pricePerUnit;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public TariffTier() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Tariff getTariff() { return tariff; }
    public void setTariff(Tariff tariff) { this.tariff = tariff; }

    public BigDecimal getMinUnits() { return minUnits; }
    public void setMinUnits(BigDecimal minUnits) { this.minUnits = minUnits; }

    public BigDecimal getMaxUnits() { return maxUnits; }
    public void setMaxUnits(BigDecimal maxUnits) { this.maxUnits = maxUnits; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
