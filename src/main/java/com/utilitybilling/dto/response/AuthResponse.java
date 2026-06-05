package com.utilitybilling.dto.response;

import java.util.List;

/**
 * Response DTO returned after successful authentication (login or register).
 * Contains JWT token and user role information.
 */
public record AuthResponse(
    String token,
    String type,
    String email,
    List<String> roles
) {}
