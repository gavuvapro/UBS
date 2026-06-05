package com.utilitybilling.repository;

import com.utilitybilling.entity.TariffTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for TariffTier entity lookups by parent tariff.
 */
@Repository
public interface TariffTierRepository extends JpaRepository<TariffTier, UUID> {
    List<TariffTier> findByTariffId(UUID tariffId);
}
