package com.utilitybilling.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating and updating customer records.
 * Validates national ID format, email, and phone number.
 */
public record CustomerRequest(
    @NotBlank(message = "Full names are required")
    @Size(min = 2, max = 255, message = "Full names must be between 2 and 255 characters")
    String fullNames,

    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "^[A-Za-z0-9]{5,20}$", message = "National ID must be alphanumeric and 5-20 characters")
    @Size(min = 5, max = 20, message = "National ID must be 5-20 characters")
    String nationalId,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number must be 8-15 digits, optionally starting with +")
    String phoneNumber,

    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address,

    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    String status
) {}
