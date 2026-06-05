package com.utilitybilling.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for capturing meter readings.
 * Validates reading values, date, and month/year constraints.
 */
public record MeterReadingRequest(
    @NotNull(message = "Meter ID is required")
    UUID meterId,

    @NotNull(message = "Previous reading is required")
    @PositiveOrZero(message = "Previous reading must be zero or positive")
    BigDecimal previousReading,

    @NotNull(message = "Current reading is required")
    @PositiveOrZero(message = "Current reading must be zero or positive")
    BigDecimal currentReading,

    @NotNull(message = "Reading date is required")
    LocalDate readingDate,

    @NotNull(message = "Reading month is required")
    @Min(value = 1, message = "Reading month must be between 1 and 12")
    @Max(value = 12, message = "Reading month must be between 1 and 12")
    Integer readingMonth,

    @NotNull(message = "Reading year is required")
    @Min(value = 2000, message = "Reading year must be 2000 or later")
    @Max(value = 2100, message = "Reading year must be 2100 or earlier")
    Integer readingYear
) {}
