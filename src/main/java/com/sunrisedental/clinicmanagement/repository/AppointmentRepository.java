package com.sunrisedental.clinicmanagement.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunrisedental.clinicmanagement.model.Appointment;
import com.sunrisedental.clinicmanagement.model.enums.AppointmentStatus;

/**
 * Provides appointment searches, schedules and reporting queries.
 */
@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    @EntityGraph(attributePaths = {"patient", "dentist", "treatment"})
    Optional<Appointment> findByAppointmentNumber(String appointmentNumber);

    boolean existsByAppointmentNumber(String appointmentNumber);

    @EntityGraph(attributePaths = {"patient", "dentist", "treatment"})
    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(
            LocalDate appointmentDate);

    @EntityGraph(attributePaths = {"patient", "dentist", "treatment"})
    List<Appointment>
            findByAppointmentDateBetweenOrderByAppointmentDateAscAppointmentTimeAsc(
                    LocalDate startDate,
                    LocalDate endDate);

    @EntityGraph(attributePaths = {"patient", "dentist", "treatment"})
    List<Appointment>
            findByDentistIdAndAppointmentDateOrderByAppointmentTimeAsc(
                    Long dentistId,
                    LocalDate appointmentDate);

    @EntityGraph(attributePaths = {"patient", "dentist", "treatment"})
    List<Appointment>
            findByDentistIdAndAppointmentDateAndStatusNotInOrderByAppointmentTimeAsc(
                    Long dentistId,
                    LocalDate appointmentDate,
                    Collection<AppointmentStatus> excludedStatuses);

    @EntityGraph(attributePaths = {"patient", "dentist", "treatment"})
    List<Appointment>
            findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(
                    Long patientId);

    @EntityGraph(attributePaths = {"patient", "dentist", "treatment"})
    List<Appointment>
            findByStatusOrderByAppointmentDateAscAppointmentTimeAsc(
                    AppointmentStatus status);

    long countByAppointmentDate(LocalDate appointmentDate);

    long countByStatus(AppointmentStatus status);
}
