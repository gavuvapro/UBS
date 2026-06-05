package com.utilitybilling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating tariff configurations.
 * Validates meter type, tariff type, and pricing fields.
 */
public record TariffRequest(
    @NotBlank(message = "Meter type is required")
    @Pattern(regexp = "^(WATER|ELECTRICITY)$", message = "Meter type must be WATER or ELECTRICITY")
    String meterType,

    @NotBlank(message = "Tariff type is required")
    @Pattern(regexp = "^(FLAT|TIER)$", message = "Tariff type must be FLAT or TIER")
    String tariffType,

    @PositiveOrZero(message = "Unit price must be zero or positive")
    BigDecimal unitPrice,

    @NotNull(message = "Effective date is required")
    LocalDate effectiveDate,

    @NotBlank(message = "Description is required")
    String description
) {}
