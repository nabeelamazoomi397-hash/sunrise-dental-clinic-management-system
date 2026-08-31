package com.sunrisedental.clinicmanagement.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunrisedental.clinicmanagement.exception.BusinessRuleException;
import com.sunrisedental.clinicmanagement.exception.ResourceNotFoundException;
import com.sunrisedental.clinicmanagement.exception.SchedulingConflictException;
import com.sunrisedental.clinicmanagement.model.Appointment;
import com.sunrisedental.clinicmanagement.model.Dentist;
import com.sunrisedental.clinicmanagement.model.Patient;
import com.sunrisedental.clinicmanagement.model.Treatment;
import com.sunrisedental.clinicmanagement.model.enums.AppointmentStatus;
import com.sunrisedental.clinicmanagement.model.enums.ReferenceType;
import com.sunrisedental.clinicmanagement.repository.AppointmentRepository;
import com.sunrisedental.clinicmanagement.repository.DentistRepository;
import com.sunrisedental.clinicmanagement.repository.PatientRepository;
import com.sunrisedental.clinicmanagement.repository.TreatmentRepository;
import com.sunrisedental.clinicmanagement.service.AppointmentService;
import com.sunrisedental.clinicmanagement.util.ReferenceNumberFactory;

@Service
public class DefaultAppointmentService
        implements AppointmentService {

    private static final LocalTime OPENING_TIME =
            LocalTime.of(8, 0);

    private static final LocalTime CLOSING_TIME =
            LocalTime.of(18, 0);

    private static final int MAX_REFERENCE_ATTEMPTS = 10;

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DentistRepository dentistRepository;
    private final TreatmentRepository treatmentRepository;
    private final ReferenceNumberFactory referenceNumberFactory;

    public DefaultAppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DentistRepository dentistRepository,
            TreatmentRepository treatmentRepository,
            ReferenceNumberFactory referenceNumberFactory) {

        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.dentistRepository = dentistRepository;
        this.treatmentRepository = treatmentRepository;
        this.referenceNumberFactory = referenceNumberFactory;
    }

    @Override
    @Transactional
    public Appointment scheduleAppointment(
            Appointment appointment,
            Long patientId,
            Long dentistId,
            Long treatmentId) {

        Patient patient = getActivePatient(patientId);
        Dentist dentist = getActiveDentist(dentistId);
        Treatment treatment = getActiveTreatment(treatmentId);

        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatment(treatment);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCancellationReason(null);
        appointment.setAppointmentNumber(
                generateUniqueAppointmentNumber());

        normalizeAppointment(appointment);
        validateSchedule(appointment, null);

        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment updateAppointment(
            Long appointmentId,
            Appointment updatedAppointment,
            Long patientId,
            Long dentistId,
            Long treatmentId) {

        Appointment existingAppointment =
                getAppointmentById(appointmentId);

        ensureAppointmentCanBeEdited(existingAppointment);

        Patient patient = getActivePatient(patientId);
        Dentist dentist = getActiveDentist(dentistId);
        Treatment treatment = getActiveTreatment(treatmentId);

        existingAppointment.setPatient(patient);
        existingAppointment.setDentist(dentist);
        existingAppointment.setTreatment(treatment);
        existingAppointment.setAppointmentDate(
                updatedAppointment.getAppointmentDate());
        existingAppointment.setAppointmentTime(
                updatedAppointment.getAppointmentTime());
        existingAppointment.setNotes(
                updatedAppointment.getNotes());

        normalizeAppointment(existingAppointment);
        validateSchedule(existingAppointment, appointmentId);

        return appointmentRepository.save(existingAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment", "ID", appointmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment getAppointmentByNumber(
            String appointmentNumber) {

        String normalizedNumber =
                appointmentNumber == null
                        ? ""
                        : appointmentNumber.trim()
                                .toUpperCase(Locale.ROOT);

        return appointmentRepository
                .findByAppointmentNumber(normalizedNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment",
                        "appointment number",
                        normalizedNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForDate(
            LocalDate appointmentDate) {

        if (appointmentDate == null) {
            throw new BusinessRuleException(
                    "Appointment date is required");
        }

        return appointmentRepository
                .findByAppointmentDateOrderByAppointmentTimeAsc(
                        appointmentDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsBetween(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new BusinessRuleException(
                    "Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException(
                    "End date cannot be before start date");
        }

        return appointmentRepository
                .findByAppointmentDateBetweenOrderByAppointmentDateAscAppointmentTimeAsc(
                        startDate,
                        endDate);
    }

    @Override
    @Transactional
    public Appointment changeAppointmentStatus(
            Long appointmentId,
            AppointmentStatus newStatus) {

        Appointment appointment =
                getAppointmentById(appointmentId);

        if (newStatus == null) {
            throw new BusinessRuleException(
                    "Appointment status is required");
        }

        if (newStatus == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Use the cancellation operation to cancel an appointment");
        }

        validateStatusTransition(
                appointment.getStatus(),
                newStatus);

        appointment.setStatus(newStatus);

        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(
            Long appointmentId,
            String cancellationReason) {

        Appointment appointment =
                getAppointmentById(appointmentId);

        if (appointment.getStatus() ==
                AppointmentStatus.COMPLETED) {

            throw new BusinessRuleException(
                    "A completed appointment cannot be cancelled");
        }

        if (appointment.getStatus() ==
                AppointmentStatus.CANCELLED) {

            throw new BusinessRuleException(
                    "This appointment is already cancelled");
        }

        if (cancellationReason == null ||
                cancellationReason.isBlank()) {

            throw new BusinessRuleException(
                    "A cancellation reason is required");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(
                cancellationReason.trim());

        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAppointmentsForDate(
            LocalDate appointmentDate) {

        if (appointmentDate == null) {
            throw new BusinessRuleException(
                    "Appointment date is required");
        }

        return appointmentRepository
                .countByAppointmentDate(appointmentDate);
    }

    private Patient getActivePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient", "ID", patientId));

        if (!patient.isActive()) {
            throw new BusinessRuleException(
                    "The selected patient is inactive");
        }

        return patient;
    }

    private Dentist getActiveDentist(Long dentistId) {
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dentist", "ID", dentistId));

        if (!dentist.isActive()) {
            throw new BusinessRuleException(
                    "The selected dentist is inactive");
        }

        return dentist;
    }

    private Treatment getActiveTreatment(Long treatmentId) {
        Treatment treatment =
                treatmentRepository.findById(treatmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Treatment",
                                        "ID",
                                        treatmentId));

        if (!treatment.isActive()) {
            throw new BusinessRuleException(
                    "The selected treatment is inactive");
        }

        return treatment;
    }

    private void validateSchedule(
            Appointment appointment,
            Long currentAppointmentId) {

        validateAppointmentDateAndTime(appointment);

        LocalTime newStart =
                appointment.getAppointmentTime();

        LocalTime newEnd = newStart.plusMinutes(
                appointment.getTreatment()
                        .getDurationMinutes());

        if (newStart.isBefore(OPENING_TIME) ||
                newEnd.isAfter(CLOSING_TIME)) {

            throw new BusinessRuleException(
                    "Appointments must be scheduled between "
                    + "08:00 and 18:00");
        }

        List<Appointment> dentistAppointments =
                appointmentRepository
                        .findByDentistIdAndAppointmentDateOrderByAppointmentTimeAsc(
                                appointment.getDentist().getId(),
                                appointment.getAppointmentDate());

        for (Appointment existing : dentistAppointments) {

            if (currentAppointmentId != null &&
                    existing.getId()
                            .equals(currentAppointmentId)) {
                continue;
            }

            if (existing.getStatus() ==
                    AppointmentStatus.CANCELLED ||
                    existing.getStatus() ==
                    AppointmentStatus.NO_SHOW) {
                continue;
            }

            LocalTime existingStart =
                    existing.getAppointmentTime();

            LocalTime existingEnd =
                    existingStart.plusMinutes(
                            existing.getTreatment()
                                    .getDurationMinutes());

            boolean overlaps =
                    newStart.isBefore(existingEnd) &&
                    existingStart.isBefore(newEnd);

            if (overlaps) {
                throw new SchedulingConflictException(
                        "The selected dentist already has "
                        + "an appointment during this time");
            }
        }
    }

    private void validateAppointmentDateAndTime(
            Appointment appointment) {

        if (appointment.getAppointmentDate() == null ||
                appointment.getAppointmentTime() == null) {

            throw new BusinessRuleException(
                    "Appointment date and time are required");
        }

        LocalDate appointmentDate =
                appointment.getAppointmentDate();

        LocalTime appointmentTime =
                appointment.getAppointmentTime();

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException(
                    "An appointment cannot be scheduled in the past");
        }

        if (appointmentDate.equals(LocalDate.now()) &&
                !appointmentTime.isAfter(LocalTime.now())) {

            throw new BusinessRuleException(
                    "An appointment cannot be scheduled in the past");
        }

        if (appointmentDate.getDayOfWeek() ==
                DayOfWeek.SUNDAY) {

            throw new BusinessRuleException(
                    "The clinic is closed on Sundays");
        }
    }

    private void validateStatusTransition(
            AppointmentStatus currentStatus,
            AppointmentStatus newStatus) {

        if (currentStatus == newStatus) {
            return;
        }

        boolean allowed = switch (currentStatus) {
            case SCHEDULED ->
                newStatus == AppointmentStatus.CONFIRMED ||
                newStatus == AppointmentStatus.NO_SHOW;

            case CONFIRMED ->
                newStatus == AppointmentStatus.IN_PROGRESS ||
                newStatus == AppointmentStatus.NO_SHOW;

            case IN_PROGRESS ->
                newStatus == AppointmentStatus.COMPLETED;

            case COMPLETED, CANCELLED, NO_SHOW -> false;
        };

        if (!allowed) {
            throw new BusinessRuleException(
                    "Appointment status cannot change from "
                    + currentStatus + " to " + newStatus);
        }
    }

    private void ensureAppointmentCanBeEdited(
            Appointment appointment) {

        if (appointment.getStatus() ==
                AppointmentStatus.COMPLETED ||
                appointment.getStatus() ==
                AppointmentStatus.CANCELLED ||
                appointment.getStatus() ==
                AppointmentStatus.NO_SHOW) {

            throw new BusinessRuleException(
                    "This appointment can no longer be edited");
        }
    }

    private String generateUniqueAppointmentNumber() {
        for (int attempt = 0;
                attempt < MAX_REFERENCE_ATTEMPTS;
                attempt++) {

            String candidate =
                    referenceNumberFactory.create(
                            ReferenceType.APPOINTMENT);

            if (!appointmentRepository
                    .existsByAppointmentNumber(candidate)) {

                return candidate;
            }
        }

        throw new BusinessRuleException(
                "Unable to generate a unique appointment number");
    }

    private void normalizeAppointment(
            Appointment appointment) {

        if (appointment.getNotes() == null ||
                appointment.getNotes().isBlank()) {

            appointment.setNotes(null);
        } else {
            appointment.setNotes(
                    appointment.getNotes().trim());
        }
    }
}