package com.sunrisedental.clinicmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunrisedental.clinicmanagement.model.Patient;

/**
 * Provides database operations for patient records.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientNumber(String patientNumber);

    Optional<Patient> findByContactNumber(String contactNumber);

    boolean existsByPatientNumber(String patientNumber);

    boolean existsByContactNumber(String contactNumber);

    List<Patient> findByFullNameContainingIgnoreCaseOrderByFullNameAsc(
            String fullName);

    List<Patient> findByActiveTrueOrderByFullNameAsc();

    long countByActiveTrue();
}