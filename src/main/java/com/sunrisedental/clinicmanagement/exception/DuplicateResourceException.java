package com.sunrisedental.clinicmanagement.exception;

/**
 * Thrown when a unique value is already used by another record.
 */
public class DuplicateResourceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(
            String resourceName,
            String fieldName,
            Object fieldValue) {
        super(String.format(
                "%s already exists with %s: %s",
                resourceName,
                fieldName,
                fieldValue
        ));
    }
}