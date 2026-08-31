package com.sunrisedental.clinicmanagement.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Provides an immutable history of important staff actions.
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_entity",
                columnList = "entity_type, entity_identifier")
    }
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_user_id")
    private StaffUser staffUser;

    /**
     * Stores a snapshot of the username even if the account is later changed.
     */
    @NotBlank(message = "Actor username is required")
    @Column(name = "actor_username", nullable = false, length = 50)
    private String actorUsername;

    @NotBlank(message = "Audit action is required")
    @Size(max = 60, message = "Action cannot exceed 60 characters")
    @Column(nullable = false, length = 60)
    private String action;

    @Size(max = 60, message = "Entity type cannot exceed 60 characters")
    @Column(name = "entity_type", length = 60)
    private String entityType;

    @Size(max = 100,
            message = "Entity identifier cannot exceed 100 characters")
    @Column(name = "entity_identifier", length = 100)
    private String entityIdentifier;

    @Size(max = 2000, message = "Audit details cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String details;

    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    protected AuditLog() {
    }

    public AuditLog(
            StaffUser staffUser,
            String actorUsername,
            String action,
            String entityType,
            String entityIdentifier,
            String details,
            String ipAddress) {
        this.staffUser = staffUser;
        this.actorUsername = actorUsername;
        this.action = action;
        this.entityType = entityType;
        this.entityIdentifier = entityIdentifier;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    @PrePersist
    protected void beforeInsert() {
        occurredAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public StaffUser getStaffUser() {
        return staffUser;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public String getDetails() {
        return details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}