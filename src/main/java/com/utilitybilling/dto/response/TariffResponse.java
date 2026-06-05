package com.utilitybilling.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for tariff configuration.
 * Includes nested tier details for tier-based tariffs.
 */
public record TariffResponse(
    UUID id,
    String meterType,
    String tariffType,
    BigDecimal unitPrice,
    LocalDate effectiveDate,
    Integer version,
    String status,
    String description,
    List<TariffTierResponse> tiers,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
