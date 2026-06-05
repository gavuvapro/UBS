package com.utilitybilling.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tariff entity defining pricing rules for water or electricity.
 * Supports flat pricing or tiered pricing (via TariffTier children).
 * Only one tariff per meter type can be active at a time.
 */
@Entity
@Table(name = "tariffs")
@EntityListeners(AuditingEntityListener.class)
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Meter.MeterType meterType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TariffType tariffType;

    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TariffStatus status = TariffStatus.ACTIVE;

    @Column
    private String description;

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TariffTier> tiers = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum TariffType {
        FLAT, TIER
    }

    public enum TariffStatus {
        ACTIVE, INACTIVE
    }

    public Tariff() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Meter.MeterType getMeterType() { return meterType; }
    public void setMeterType(Meter.MeterType meterType) { this.meterType = meterType; }

    public TariffType getTariffType() { return tariffType; }
    public void setTariffType(TariffType tariffType) { this.tariffType = tariffType; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public TariffStatus getStatus() { return status; }
    public void setStatus(TariffStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<TariffTier> getTiers() { return tiers; }
    public void setTiers(List<TariffTier> tiers) { this.tiers = tiers; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
