package com.utilitybilling.exception;

/**
 * Exception thrown when a business rule is violated (e.g., duplicate national ID, inactive meter).
 * Results in an HTTP 400 response via the GlobalExceptionHandler.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
