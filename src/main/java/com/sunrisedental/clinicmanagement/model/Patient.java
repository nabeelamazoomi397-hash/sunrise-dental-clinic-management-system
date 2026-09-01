package com.sunrisedental.clinicmanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sunrisedental.clinicmanagement.model.enums.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Stores the personal and medical registration details of a clinic patient.
 */
@Entity
@Table(
    name = "patients",
    indexes = {
        @Index(name = "idx_patient_number", columnList = "patient_number"),
        @Index(name = "idx_patient_contact", columnList = "contact_number")
    }
)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_number", nullable = false, unique = true, length = 25)
    private String patientNumber;

    @NotBlank(message = "Patient name is required")
    @Size(max = 100, message = "Patient name cannot exceed 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String address;

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

    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private Gender gender;

    @Size(max = 1000, message = "Medical notes cannot exceed 1000 characters")
    @Column(name = "medical_notes", columnDefinition = "TEXT")
    private String medicalNotes;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Required by JPA when reconstructing a patient from the database.
     */
    public Patient() {
    }

    /**
     * Parameterized constructor used when registering a new patient.
     */
    public Patient(
            String fullName,
            String address,
            String contactNumber,
            String email,
            LocalDate dateOfBirth,
            Gender gender,
            String medicalNotes) {
        this.fullName = fullName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.medicalNotes = medicalNotes;
    }

    /**
     * Overloaded constructor used when importing a patient with an existing
     * patient number.
     */
    public Patient(
            String patientNumber,
            String fullName,
            String address,
            String contactNumber,
            String email,
            LocalDate dateOfBirth,
            Gender gender,
            String medicalNotes) {
        this(fullName, address, contactNumber, email,
                dateOfBirth, gender, medicalNotes);
        this.patientNumber = patientNumber;
    }

    @PrePersist
    protected void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        registeredAt = now;
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

	public String getPatientNumber() {
		return patientNumber;
	}

	public void setPatientNumber(String patientNumber) {
		this.patientNumber = patientNumber;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getMedicalNotes() {
		return medicalNotes;
	}

	public void setMedicalNotes(String medicalNotes) {
		this.medicalNotes = medicalNotes;
	}

	public LocalDateTime getRegisteredAt() {
		return registeredAt;
	}

	public void setRegisteredAt(LocalDateTime registeredAt) {
		this.registeredAt = registeredAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}