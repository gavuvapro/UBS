package com.utilitybilling.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for adding tariff tiers to tier-based tariffs.
 * Validates unit ranges and pricing.
 */
public record TariffTierRequest(
    @NotNull(message = "Tariff ID is required")
    UUID tariffId,

    @NotNull(message = "Min units is required")
    @PositiveOrZero(message = "Min units must be zero or positive")
    BigDecimal minUnits,

    @PositiveOrZero(message = "Max units must be zero or positive")
    BigDecimal maxUnits,

    @NotNull(message = "Price per unit is required")
    @PositiveOrZero(message = "Price per unit must be zero or positive")
    BigDecimal pricePerUnit
) {}
