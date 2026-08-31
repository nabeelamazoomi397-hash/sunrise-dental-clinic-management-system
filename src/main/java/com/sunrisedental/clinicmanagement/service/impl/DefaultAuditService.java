package com.sunrisedental.clinicmanagement.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunrisedental.clinicmanagement.exception.BusinessRuleException;
import com.sunrisedental.clinicmanagement.model.AuditLog;
import com.sunrisedental.clinicmanagement.model.StaffUser;
import com.sunrisedental.clinicmanagement.model.enums.ReferenceType;
import com.sunrisedental.clinicmanagement.repository.AuditLogRepository;
import com.sunrisedental.clinicmanagement.service.AuditService;

@Service
public class DefaultAuditService implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public DefaultAuditService(
            AuditLogRepository auditLogRepository) {

        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public AuditLog recordAction(
            StaffUser staffUser,
            String actorUsername,
            String action,
            ReferenceType referenceType,
            Long referenceId,
            String details,
            String ipAddress) {

        if (action == null || action.isBlank()) {
            throw new BusinessRuleException(
                    "Audit action is required");
        }

        if (referenceType == null) {
            throw new BusinessRuleException(
                    "Audit reference type is required");
        }

        String safeActorUsername =
                actorUsername == null ||
                actorUsername.isBlank()
                        ? "SYSTEM"
                        : actorUsername.trim();

        String entityIdentifier =
                referenceId == null
                        ? null
                        : referenceId.toString();

        AuditLog auditLog = new AuditLog(
                staffUser,
                safeActorUsername,
                action.trim(),
                referenceType.name(),
                entityIdentifier,
                trimToNull(details),
                trimToNull(ipAddress));

        return auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(
            Pageable pageable) {

        if (pageable == null) {
            throw new BusinessRuleException(
                    "Pagination information is required");
        }

        return auditLogRepository.findAll(pageable);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}