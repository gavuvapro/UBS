package com.utilitybilling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating and updating meter records.
 * Validates meter number uniqueness constraints and meter type values.
 */
public record MeterRequest(
    @NotBlank(message = "Meter number is required")
    @Size(min = 1, max = 100, message = "Meter number must be 1-100 characters")
    String meterNumber,

    @NotNull(message = "Meter type is required")
    @Pattern(regexp = "^(WATER|ELECTRICITY)$", message = "Meter type must be WATER or ELECTRICITY")
    String meterType,

    @NotNull(message = "Installation date is required")
    LocalDate installationDate,

    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    String status,

    @NotNull(message = "Customer ID is required")
    UUID customerId
) {}
