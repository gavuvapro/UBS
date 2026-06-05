package com.utilitybilling.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for meter data.
 * Includes associated customer information for display purposes.
 */
public record MeterResponse(
    UUID id,
    String meterNumber,
    String meterType,
    LocalDate installationDate,
    String status,
    UUID customerId,
    String customerName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
