package com.utilitybilling.exception;

/**
 * Exception thrown when a requested resource (entity) does not exist in the database.
 * Typically results in an HTTP 404 response via the GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s : '%s'", resource, field, value));
    }
}
