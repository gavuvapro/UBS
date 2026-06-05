package com.utilitybilling.repository;

import com.utilitybilling.entity.Meter;
import com.utilitybilling.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Tariff entity.
 * Supports version tracking per meter type and active tariff lookups.
 */
@Repository
public interface TariffRepository extends JpaRepository<Tariff, UUID> {

    @Query("SELECT MAX(t.version) FROM Tariff t WHERE t.meterType = :meterType")
    Optional<Integer> findMaxVersionByMeterType(@Param("meterType") Meter.MeterType meterType);

    Optional<Tariff> findByMeterTypeAndStatus(Meter.MeterType meterType, Tariff.TariffStatus status);

    boolean existsByMeterTypeAndStatus(Meter.MeterType meterType, Tariff.TariffStatus status);
}
