package com.sunrisedental.clinicmanagement.exception;

/**
 * Thrown when an operation violates a clinic business rule.
 */
public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessRuleException(String message) {
        super(message);
    }
}