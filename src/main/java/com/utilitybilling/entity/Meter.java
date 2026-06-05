package com.utilitybilling.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Meter entity representing physical utility meters (water or electricity).
 * Each meter belongs to a customer and can have multiple readings over time.
 */
@Entity
@Table(name = "meters")
@EntityListeners(AuditingEntityListener.class)
public class Meter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String meterNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MeterType meterType;

    @Column
    private LocalDate installationDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MeterStatus status = MeterStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @OneToMany(mappedBy = "meter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeterReading> meterReadings = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum MeterType {
        WATER, ELECTRICITY
    }

    public enum MeterStatus {
        ACTIVE, INACTIVE
    }

    public Meter() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getMeterNumber() { return meterNumber; }
    public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }

    public MeterType getMeterType() { return meterType; }
    public void setMeterType(MeterType meterType) { this.meterType = meterType; }

    public LocalDate getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }

    public MeterStatus getStatus() { return status; }
    public void setStatus(MeterStatus status) { this.status = status; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public List<MeterReading> getMeterReadings() { return meterReadings; }
    public void setMeterReadings(List<MeterReading> meterReadings) { this.meterReadings = meterReadings; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
