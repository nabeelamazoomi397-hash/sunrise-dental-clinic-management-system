package com.sunrisedental.clinicmanagement.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunrisedental.clinicmanagement.model.AuditLog;

/**
 * Provides paginated searches of the system audit history.
 */
@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByOccurredAtDesc(Pageable pageable);

    Page<AuditLog>
            findByOccurredAtBetweenOrderByOccurredAtDesc(
                    LocalDateTime start,
                    LocalDateTime end,
                    Pageable pageable);

    Page<AuditLog>
            findByActorUsernameContainingIgnoreCaseOrderByOccurredAtDesc(
                    String actorUsername,
                    Pageable pageable);

    Page<AuditLog>
            findByActionContainingIgnoreCaseOrderByOccurredAtDesc(
                    String action,
                    Pageable pageable);
}