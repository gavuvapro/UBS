package com.utilitybilling.repository;

import com.utilitybilling.entity.TaxConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for TaxConfig entity lookups by active status.
 */
@Repository
public interface TaxConfigRepository extends JpaRepository<TaxConfig, UUID> {
    Optional<TaxConfig> findByStatus(TaxConfig.TaxStatus status);
}
