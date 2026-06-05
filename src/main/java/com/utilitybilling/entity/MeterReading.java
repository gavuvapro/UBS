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
 * Meter reading entity capturing monthly consumption data.
 * Consumption is computed as currentReading minus previousReading.
 */
@Entity
@Table(name = "meter_readings")
@EntityListeners(AuditingEntityListener.class)
public class MeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meter_id", referencedColumnName = "id", nullable = false)
    private Meter meter;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal previousReading = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal currentReading;

    @Column(nullable = false)
    private LocalDate readingDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal consumption;

    @Column(nullable = false)
    private Integer readingMonth;

    @Column(nullable = false)
    private Integer readingYear;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public MeterReading() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Meter getMeter() { return meter; }
    public void setMeter(Meter meter) { this.meter = meter; }

    public BigDecimal getPreviousReading() { return previousReading; }
    public void setPreviousReading(BigDecimal previousReading) { this.previousReading = previousReading; }

    public BigDecimal getCurrentReading() { return currentReading; }
    public void setCurrentReading(BigDecimal currentReading) { this.currentReading = currentReading; }

    public LocalDate getReadingDate() { return readingDate; }
    public void setReadingDate(LocalDate readingDate) { this.readingDate = readingDate; }

    public BigDecimal getConsumption() { return consumption; }
    public void setConsumption(BigDecimal consumption) { this.consumption = consumption; }

    public Integer getReadingMonth() { return readingMonth; }
    public void setReadingMonth(Integer readingMonth) { this.readingMonth = readingMonth; }

    public Integer getReadingYear() { return readingYear; }
    public void setReadingYear(Integer readingYear) { this.readingYear = readingYear; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
