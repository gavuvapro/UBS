package com.utilitybilling.repository;

import com.utilitybilling.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Meter entity lookups by meter number and customer.
 */
@Repository
public interface MeterRepository extends JpaRepository<Meter, UUID> {
    Optional<Meter> findByMeterNumber(String meterNumber);
    boolean existsByMeterNumber(String meterNumber);
    List<Meter> findByCustomerId(UUID customerId);
}
