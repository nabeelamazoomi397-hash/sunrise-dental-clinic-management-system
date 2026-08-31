package com.sunrisedental.clinicmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunrisedental.clinicmanagement.model.StaffUser;
import com.sunrisedental.clinicmanagement.model.enums.Role;

/**
 * Provides secure staff-account searches and management operations.
 */
@Repository
public interface StaffUserRepository
        extends JpaRepository<StaffUser, Long> {

    Optional<StaffUser> findByUsernameIgnoreCase(String username);

    Optional<StaffUser> findByEmployeeNumber(String employeeNumber);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmployeeNumber(String employeeNumber);

    List<StaffUser> findByRoleOrderByFullNameAsc(Role role);

    List<StaffUser> findByActiveTrueOrderByFullNameAsc();

    long countByActiveTrue();
}