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
 * ServiceCharge entity representing fixed charges applied to bills (e.g., maintenance fee).
 * Separate active configurations exist per meter type (water or electricity).
 */
@Entity
@Table(name = "service_charges")
@EntityListeners(AuditingEntityListener.class)
public class ServiceCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Meter.MeterType meterType;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChargeStatus status = ChargeStatus.ACTIVE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ChargeStatus {
        ACTIVE, INACTIVE
    }

    public ServiceCharge() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Meter.MeterType getMeterType() { return meterType; }
    public void setMeterType(Meter.MeterType meterType) { this.meterType = meterType; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public ChargeStatus getStatus() { return status; }
    public void setStatus(ChargeStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
