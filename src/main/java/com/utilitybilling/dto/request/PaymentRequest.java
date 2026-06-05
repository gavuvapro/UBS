package com.utilitybilling.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for recording payments against bills.
 * Validates payment amount, method, and required associations.
 */
public record PaymentRequest(
    @NotNull(message = "Bill ID is required")
    UUID billId,

    @NotNull(message = "Amount paid is required")
    @Positive(message = "Amount paid must be greater than zero")
    BigDecimal amountPaid,

    @NotNull(message = "Payment method is required")
    @Pattern(regexp = "^(CASH|BANK_TRANSFER|MOBILE_MONEY)$", message = "Payment method must be CASH, BANK_TRANSFER, or MOBILE_MONEY")
    String paymentMethod,

    @NotNull(message = "Payment date is required")
    LocalDate paymentDate
) {}
