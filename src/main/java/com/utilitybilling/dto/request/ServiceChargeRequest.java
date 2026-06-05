package com.utilitybilling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating service charge configurations.
 * Validates amount, meter type, and effective date.
 */
public record ServiceChargeRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "Amount is required")
    @PositiveOrZero(message = "Amount must be zero or positive")
    BigDecimal amount,

    @NotBlank(message = "Meter type is required")
    @Pattern(regexp = "^(WATER|ELECTRICITY)$", message = "Meter type must be WATER or ELECTRICITY")
    String meterType,

    @NotNull(message = "Effective date is required")
    LocalDate effectiveDate
) {}
