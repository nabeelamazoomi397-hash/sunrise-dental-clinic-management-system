package com.sunrisedental.clinicmanagement.exception;

/**
 * Thrown when an appointment conflicts with a dentist's existing schedule.
 */
public class SchedulingConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SchedulingConflictException(String message) {
        super(message);
    }
}