package com.sunrisedental.clinicmanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.sunrisedental.clinicmanagement.model.enums.AppointmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents one scheduled patient visit at Sunrise Dental Clinic.
 */
@Entity
@Table(
    name = "appointments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_dentist_schedule",
            columnNames = {
                "dentist_id",
                "appointment_date",
                "appointment_time"
            }
        )
    },
    indexes = {
        @Index(name = "idx_appointment_number",
                columnList = "appointment_number"),
        @Index(name = "idx_appointment_date",
                columnList = "appointment_date"),
        @Index(name = "idx_appointment_status",
                columnList = "status"),
        @Index(name = "idx_dentist_date",
                columnList = "dentist_id, appointment_date")
    }
)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Appointment number is required")
    @Column(
        name = "appointment_number",
        nullable = false,
        unique = true,
        length = 30
    )
    private String appointmentNumber;

    @NotNull(message = "Patient is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull(message = "Dentist is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dentist_id", nullable = false)
    private Dentist dentist;

    @NotNull(message = "Treatment is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treatment_id", nullable = false)
    private Treatment treatment;

    @NotNull(message = "Appointment date is required")
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required")
    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @NotNull(message = "Appointment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Size(
        max = 500,
        message = "Cancellation reason cannot exceed 500 characters"
    )
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Prevents two staff members from silently overwriting the same record.
     */
    @Version
    @Column(nullable = false)
    private long version;

    protected Appointment() {
    }

    public Appointment(
            Patient patient,
            Dentist dentist,
            Treatment treatment,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String notes) {
        this.patient = patient;
        this.dentist = dentist;
        this.treatment = treatment;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.notes = notes;
    }

    public Appointment(
            String appointmentNumber,
            Patient patient,
            Dentist dentist,
            Treatment treatment,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String notes) {
        this(
            patient,
            dentist,
            treatment,
            appointmentDate,
            appointmentTime,
            notes
        );
        this.appointmentNumber = appointmentNumber;
    }

    @PrePersist
    protected void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = AppointmentStatus.SCHEDULED;
        }
    }

    @PreUpdate
    protected void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Dentist getDentist() {
        return dentist;
    }

    public void setDentist(Dentist dentist) {
        this.dentist = dentist;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}