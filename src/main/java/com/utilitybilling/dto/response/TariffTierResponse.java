package com.utilitybilling.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for a single tariff tier band.
 * Used within TariffResponse for tier-based pricing.
 */
public record TariffTierResponse(
    UUID id,
    UUID tariffId,
    BigDecimal minUnits,
    BigDecimal maxUnits,
    BigDecimal pricePerUnit
) {}
