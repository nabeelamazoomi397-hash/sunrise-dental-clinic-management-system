package com.sunrisedental.clinicmanagement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sunrisedental.clinicmanagement.model.AuditLog;
import com.sunrisedental.clinicmanagement.model.StaffUser;
import com.sunrisedental.clinicmanagement.model.enums.ReferenceType;

public interface AuditService {

    AuditLog recordAction(
            StaffUser staffUser,
            String actorUsername,
            String action,
            ReferenceType referenceType,
            Long referenceId,
            String details,
            String ipAddress);

    Page<AuditLog> getAuditLogs(Pageable pageable);
}