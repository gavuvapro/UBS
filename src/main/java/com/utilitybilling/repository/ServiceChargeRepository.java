package com.utilitybilling.repository;

import com.utilitybilling.entity.Meter;
import com.utilitybilling.entity.ServiceCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for ServiceCharge entity lookups by meter type and active status.
 */
@Repository
public interface ServiceChargeRepository extends JpaRepository<ServiceCharge, UUID> {
    Optional<ServiceCharge> findByMeterTypeAndStatus(Meter.MeterType meterType, ServiceCharge.ChargeStatus status);
}
