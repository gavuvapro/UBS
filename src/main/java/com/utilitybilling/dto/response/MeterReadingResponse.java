package com.utilitybilling.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for meter reading captures.
 * Shows computed consumption and meter details.
 */
public record MeterReadingResponse(
    UUID id,
    UUID meterId,
    String meterNumber,
    BigDecimal previousReading,
    BigDecimal currentReading,
    BigDecimal consumption,
    LocalDate readingDate,
    Integer readingMonth,
    Integer readingYear,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
