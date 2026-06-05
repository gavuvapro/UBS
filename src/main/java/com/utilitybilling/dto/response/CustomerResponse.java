package com.utilitybilling.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for customer data.
 * Exposes customer profile and status information.
 */
public record CustomerResponse(
    UUID id,
    String fullNames,
    String nationalId,
    String email,
    String phoneNumber,
    String address,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
