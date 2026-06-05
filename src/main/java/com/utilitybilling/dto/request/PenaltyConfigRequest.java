package com.utilitybilling.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating penalty configurations.
 * Validates penalty type, value, and grace period.
 */
public record PenaltyConfigRequest(
    @NotNull(message = "Penalty type is required")
    @Pattern(regexp = "^(FIXED|PERCENTAGE)$", message = "Penalty type must be FIXED or PERCENTAGE")
    String penaltyType,

    @NotNull(message = "Value is required")
    @PositiveOrZero(message = "Value must be zero or positive")
    BigDecimal value,

    @NotNull(message = "Grace period days is required")
    @Min(value = 0, message = "Grace period days must be zero or positive")
    Integer gracePeriodDays,

    @NotNull(message = "Effective date is required")
    LocalDate effectiveDate
) {}
