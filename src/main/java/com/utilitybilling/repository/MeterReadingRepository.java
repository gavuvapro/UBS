package com.utilitybilling.repository;

import com.utilitybilling.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for MeterReading entity.
 * Enforces month/year uniqueness per meter via existence checks.
 */
@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    boolean existsByMeterIdAndReadingMonthAndReadingYear(UUID meterId, Integer readingMonth, Integer readingYear);
    Optional<MeterReading> findByMeterIdAndReadingMonthAndReadingYear(UUID meterId, Integer readingMonth, Integer readingYear);
}
