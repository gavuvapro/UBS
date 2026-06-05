package com.utilitybilling.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for generated bills.
 * Breaks down all charge components: consumption, service, tax, and penalties.
 */
public record BillResponse(
    UUID id,
    String billReference,
    UUID customerId,
    String customerName,
    UUID meterId,
    String meterNumber,
    UUID meterReadingId,
    Integer billingMonth,
    Integer billingYear,
    BigDecimal consumption,
    BigDecimal consumptionCharge,
    BigDecimal serviceCharge,
    BigDecimal taxAmount,
    BigDecimal penaltyAmount,
    BigDecimal totalAmount,
    BigDecimal amountPaid,
    BigDecimal outstandingBalance,
    String status,
    LocalDateTime generatedAt,
    LocalDateTime approvedAt,
    UUID approvedById,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
