package com.sunrisedental.clinicmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunrisedental.clinicmanagement.model.Treatment;

/**
 * Provides database operations for treatment records and prices.
 */
@Repository
public interface TreatmentRepository
        extends JpaRepository<Treatment, Long> {

    Optional<Treatment> findByTreatmentCode(String treatmentCode);

    Optional<Treatment> findByNameIgnoreCase(String name);

    boolean existsByTreatmentCode(String treatmentCode);

    boolean existsByNameIgnoreCase(String name);

    List<Treatment> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<Treatment> findByActiveTrueOrderByNameAsc();

    long countByActiveTrue();
}