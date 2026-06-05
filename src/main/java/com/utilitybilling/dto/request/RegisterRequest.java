package com.utilitybilling.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration requests.
 * Validates email format, password length, and phone number format.
 */
public record RegisterRequest(
    @NotBlank(message = "Full names are required")
    @Size(min = 2, max = 255, message = "Full names must be between 2 and 255 characters")
    String fullNames,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number must be 8-15 digits, optionally starting with +")
    String phoneNumber,

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    String password
) {}
