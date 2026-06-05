package com.utilitybilling.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating tax configurations (e.g., VAT).
 * Validates tax rate between 0 and 1 (0% to 100%).
 */
public record TaxConfigRequest(
    @NotBlank(message = "Tax name is required")
    String taxName,

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Rate must be at least 0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Rate must not exceed 1.0 (100%)")
    BigDecimal rate,

    @NotNull(message = "Effective date is required")
    LocalDate effectiveDate
) {}
