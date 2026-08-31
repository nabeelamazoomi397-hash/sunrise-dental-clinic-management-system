package com.sunrisedental.clinicmanagement.service;

import java.time.LocalDate;
import java.util.List;

import com.sunrisedental.clinicmanagement.model.Appointment;
import com.sunrisedental.clinicmanagement.model.enums.AppointmentStatus;

public interface AppointmentService {

    Appointment scheduleAppointment(
            Appointment appointment,
            Long patientId,
            Long dentistId,
            Long treatmentId);

    Appointment updateAppointment(
            Long appointmentId,
            Appointment updatedAppointment,
            Long patientId,
            Long dentistId,
            Long treatmentId);

    Appointment getAppointmentById(Long appointmentId);

    Appointment getAppointmentByNumber(
            String appointmentNumber);

    List<Appointment> getAppointmentsForDate(
            LocalDate appointmentDate);

    List<Appointment> getAppointmentsBetween(
            LocalDate startDate,
            LocalDate endDate);

    Appointment changeAppointmentStatus(
            Long appointmentId,
            AppointmentStatus newStatus);

    Appointment cancelAppointment(
            Long appointmentId,
            String cancellationReason);

    long countAppointmentsForDate(
            LocalDate appointmentDate);
}