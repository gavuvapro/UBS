package com.utilitybilling.repository;

import com.utilitybilling.entity.PenaltyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA repository for PenaltyConfig entity persistence.
 */
@Repository
public interface PenaltyConfigRepository extends JpaRepository<PenaltyConfig, UUID> {
}
