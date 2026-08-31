package com.sunrisedental.clinicmanagement.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sunrisedental.clinicmanagement.model.enums.Role;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents an authorized clinic staff account.
 */
@Entity
@Table(
    name = "staff_users",
    indexes = {
        @Index(name = "idx_staff_employee_number",
                columnList = "employee_number"),
        @Index(name = "idx_staff_username",
                columnList = "username"),
        @Index(name = "idx_staff_role",
                columnList = "role")
    }
)
public class StaffUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Employee number is required")
    @Column(
        name = "employee_number",
        nullable = false,
        unique = true,
        length = 20
    )
    private String employeeNumber;

    @NotBlank(message = "Staff name is required")
    @Size(max = 100, message = "Staff name cannot exceed 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50,
            message = "Username must contain between 4 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Stores only a BCrypt hash. A plain-text password is never persisted.
     */
    @JsonIgnore
    @NotBlank(message = "Password hash is required")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Email(message = "Enter a valid email address")
    @Size(max = 120, message = "Email cannot exceed 120 characters")
    @Column(length = 120)
    private String email;

    @NotNull(message = "Staff role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private Role role;

    /**
     * Links a DENTIST login to its professional dentist record.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", unique = true)
    private Dentist dentist;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected StaffUser() {
    }

    public StaffUser(
            String fullName,
            String username,
            String passwordHash,
            String email,
            Role role) {
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
    }

    public StaffUser(
            String employeeNumber,
            String fullName,
            String username,
            String passwordHash,
            String email,
            Role role) {
        this(fullName, username, passwordHash, email, role);
        this.employeeNumber = employeeNumber;
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

    public void recordSuccessfulLogin() {
        lastLoginAt = LocalDateTime.now();
        failedLoginAttempts = 0;
    }

    public void recordFailedLogin() {
        failedLoginAttempts++;

        if (failedLoginAttempts >= 5) {
            accountLocked = true;
        }
    }

    public void unlockAccount() {
        accountLocked = false;
        failedLoginAttempts = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Dentist getDentist() {
        return dentist;
    }

    public void setDentist(Dentist dentist) {
        this.dentist = dentist;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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
