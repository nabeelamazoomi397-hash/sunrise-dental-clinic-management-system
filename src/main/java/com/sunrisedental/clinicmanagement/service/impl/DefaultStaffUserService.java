package com.sunrisedental.clinicmanagement.service.impl;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunrisedental.clinicmanagement.exception.BusinessRuleException;
import com.sunrisedental.clinicmanagement.exception.ResourceNotFoundException;
import com.sunrisedental.clinicmanagement.model.StaffUser;
import com.sunrisedental.clinicmanagement.model.enums.ReferenceType;
import com.sunrisedental.clinicmanagement.model.enums.Role;
import com.sunrisedental.clinicmanagement.repository.StaffUserRepository;
import com.sunrisedental.clinicmanagement.service.StaffUserService;
import com.sunrisedental.clinicmanagement.util.ReferenceNumberFactory;

@Service
public class DefaultStaffUserService
        implements StaffUserService {

    private static final int MAX_REFERENCE_ATTEMPTS = 10;

    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReferenceNumberFactory referenceNumberFactory;

    public DefaultStaffUserService(
            StaffUserRepository staffUserRepository,
            PasswordEncoder passwordEncoder,
            ReferenceNumberFactory referenceNumberFactory) {

        this.staffUserRepository = staffUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.referenceNumberFactory = referenceNumberFactory;
    }

    @Override
    @Transactional
    public StaffUser createStaffUser(
            StaffUser staffUser,
            String rawPassword) {

        normalizeStaffUser(staffUser);
        validateNewUsername(staffUser.getUsername());
        validatePassword(rawPassword);

        staffUser.setEmployeeNumber(
                generateUniqueEmployeeNumber());

        staffUser.setPasswordHash(
                passwordEncoder.encode(rawPassword));

        staffUser.setActive(true);
        staffUser.setAccountLocked(false);
        staffUser.setFailedLoginAttempts(0);

        return staffUserRepository.save(staffUser);
    }

    @Override
    @Transactional
    public StaffUser updateStaffUser(
            Long staffUserId,
            StaffUser updatedStaffUser) {

        StaffUser existingUser =
                getStaffUserById(staffUserId);

        String normalizedUsername =
                normalizeUsername(
                        updatedStaffUser.getUsername());

        staffUserRepository
                .findByUsernameIgnoreCase(normalizedUsername)
                .ifPresent(userWithSameUsername -> {

                    if (!userWithSameUsername.getId()
                            .equals(staffUserId)) {

                        throw new BusinessRuleException(
                                "This username is already in use");
                    }
                });

        existingUser.setFullName(
                updatedStaffUser.getFullName());

        existingUser.setUsername(normalizedUsername);
        existingUser.setEmail(updatedStaffUser.getEmail());
        existingUser.setRole(updatedStaffUser.getRole());

        existingUser.setDentist(
                updatedStaffUser.getDentist());

        normalizeStaffUser(existingUser);

        return staffUserRepository.save(existingUser);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffUser getStaffUserById(Long staffUserId) {
        return staffUserRepository.findById(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff user",
                        "ID",
                        staffUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public StaffUser getStaffUserByUsername(
            String username) {

        String normalizedUsername =
                normalizeUsername(username);

        return staffUserRepository
                .findByUsernameIgnoreCase(normalizedUsername)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff user",
                        "username",
                        normalizedUsername));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffUser> getActiveStaffUsers() {
        return staffUserRepository
                .findByActiveTrueOrderByFullNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffUser> getActiveStaffUsersByRole(
            Role role) {

        if (role == null) {
            throw new BusinessRuleException(
                    "Staff role is required");
        }

        return staffUserRepository
                .findByActiveTrueOrderByFullNameAsc()
                .stream()
                .filter(user -> user.getRole() == role)
                .toList();
    }

    @Override
    @Transactional
    public StaffUser unlockStaffUser(Long staffUserId) {
        StaffUser staffUser =
                getStaffUserById(staffUserId);

        staffUser.setAccountLocked(false);
        staffUser.setFailedLoginAttempts(0);

        return staffUserRepository.save(staffUser);
    }

    @Override
    @Transactional
    public void deactivateStaffUser(Long staffUserId) {
        StaffUser staffUser =
                getStaffUserById(staffUserId);

        staffUser.setActive(false);
        staffUserRepository.save(staffUser);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveStaffUsers() {
        return staffUserRepository.countByActiveTrue();
    }

    private void validateNewUsername(String username) {
        if (staffUserRepository
                .existsByUsernameIgnoreCase(username)) {

            throw new BusinessRuleException(
                    "This username is already in use");
        }
    }

    private void validatePassword(String rawPassword) {
        if (rawPassword == null ||
                rawPassword.length() < 8) {

            throw new BusinessRuleException(
                    "Password must contain at least 8 characters");
        }

        boolean hasUppercase =
                rawPassword.chars()
                        .anyMatch(Character::isUpperCase);

        boolean hasLowercase =
                rawPassword.chars()
                        .anyMatch(Character::isLowerCase);

        boolean hasDigit =
                rawPassword.chars()
                        .anyMatch(Character::isDigit);

        if (!hasUppercase ||
                !hasLowercase ||
                !hasDigit) {

            throw new BusinessRuleException(
                    "Password must contain uppercase, "
                    + "lowercase and numeric characters");
        }
    }

    private String generateUniqueEmployeeNumber() {
        for (int attempt = 0;
                attempt < MAX_REFERENCE_ATTEMPTS;
                attempt++) {

            String candidate =
                    referenceNumberFactory.create(
                            ReferenceType.STAFF);

            if (!staffUserRepository
                    .existsByEmployeeNumber(candidate)) {

                return candidate;
            }
        }

        throw new BusinessRuleException(
                "Unable to generate a unique employee number");
    }

    private void normalizeStaffUser(
            StaffUser staffUser) {

        staffUser.setFullName(
                trim(staffUser.getFullName()));

        staffUser.setUsername(
                normalizeUsername(
                        staffUser.getUsername()));

        if (staffUser.getEmail() == null ||
                staffUser.getEmail().isBlank()) {

            staffUser.setEmail(null);
        } else {
            staffUser.setEmail(
                    staffUser.getEmail()
                            .trim()
                            .toLowerCase(Locale.ROOT));
        }

        if (staffUser.getRole() == null) {
            throw new BusinessRuleException(
                    "Staff role is required");
        }
    }

    private String normalizeUsername(String username) {
        return username == null
                ? ""
                : username.trim()
                        .toLowerCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}