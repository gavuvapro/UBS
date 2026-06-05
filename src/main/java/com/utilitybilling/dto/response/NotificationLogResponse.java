package com.utilitybilling.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for notification logs.
 * Tracks system-generated notifications sent to customers.
 */
public record NotificationLogResponse(
    UUID id,
    UUID customerId,
    String customerName,
    String message,
    String notificationType,
    LocalDateTime createdAt,
    boolean sent
) {}
