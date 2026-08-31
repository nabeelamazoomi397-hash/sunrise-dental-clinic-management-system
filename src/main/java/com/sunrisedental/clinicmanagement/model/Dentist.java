package com.sunrisedental.clinicmanagement.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Stores the professional and contact details of a clinic dentist.
 */
@Entity
@Table(
    name = "dentists",
    indexes = {
        @Index(name = "idx_dentist_code", columnList = "dentist_code"),
        @Index(name = "idx_dentist_name", columnList = "full_name")
    }
)
public class Dentist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Dentist code is required")
    @Column(name = "dentist_code", nullable = false, unique = true, length = 20)
    private String dentistCode;

    @NotBlank(message = "Dentist name is required")
    @Size(max = 100, message = "Dentist name cannot exceed 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100, message = "Specialization cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String specialization;

    @NotBlank(message = "Contact number is required")
    @Pattern(
        regexp = "^(?:\\+94|0)7\\d{8}$",
        message = "Enter a valid Sri Lankan mobile number"
    )
    @Column(name = "contact_number", nullable = false, length = 15)
    private String contactNumber;

    @Email(message = "Enter a valid email address")
    @Size(max = 120, message = "Email cannot exceed 120 characters")
    @Column(length = 120)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Dentist() {
    }

    public Dentist(
            String fullName,
            String specialization,
            String contactNumber,
            String email) {
        this.fullName = fullName;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    public Dentist(
            String dentistCode,
            String fullName,
            String specialization,
            String contactNumber,
            String email) {
        this(fullName, specialization, contactNumber, email);
        this.dentistCode = dentistCode;
    }

    @PrePersist
    protected void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
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

    public String getDentistCode() {
        return dentistCode;
    }

    public void setDentistCode(String dentistCode) {
        this.dentistCode = dentistCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
}