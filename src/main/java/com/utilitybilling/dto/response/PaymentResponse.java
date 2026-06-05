package com.utilitybilling.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for recorded payments.
 * Links back to the associated bill and shows payment method.
 */
public record PaymentResponse(
    UUID id,
    UUID billId,
    String billReference,
    BigDecimal amountPaid,
    String paymentMethod,
    LocalDate paymentDate,
    String reference,
    UUID recordedById,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
