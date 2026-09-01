package com.sunrisedental.clinicmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunrisedental.clinicmanagement.model.Dentist;

/**
 * Provides database operations for dentist records.
 */
@Repository
public interface DentistRepository extends JpaRepository<Dentist, Long> {

    Optional<Dentist> findByDentistCode(String dentistCode);

    Optional<Dentist> findByEmailIgnoreCase(String email);

    boolean existsByDentistCode(String dentistCode);

    List<Dentist> findByFullNameContainingIgnoreCaseOrderByFullNameAsc(
            String fullName);

    List<Dentist> findByActiveTrueOrderByFullNameAsc();

    long countByActiveTrue();
}