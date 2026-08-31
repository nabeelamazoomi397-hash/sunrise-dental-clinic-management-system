package com.sunrisedental.clinicmanagement.service.impl;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunrisedental.clinicmanagement.exception.BusinessRuleException;
import com.sunrisedental.clinicmanagement.exception.ResourceNotFoundException;
import com.sunrisedental.clinicmanagement.model.Dentist;
import com.sunrisedental.clinicmanagement.model.enums.ReferenceType;
import com.sunrisedental.clinicmanagement.repository.DentistRepository;
import com.sunrisedental.clinicmanagement.service.DentistService;
import com.sunrisedental.clinicmanagement.util.ReferenceNumberFactory;

@Service
public class DefaultDentistService implements DentistService {

    private static final int MAX_REFERENCE_ATTEMPTS = 10;

    private final DentistRepository dentistRepository;
    private final ReferenceNumberFactory referenceNumberFactory;

    public DefaultDentistService(
            DentistRepository dentistRepository,
            ReferenceNumberFactory referenceNumberFactory) {

        this.dentistRepository = dentistRepository;
        this.referenceNumberFactory = referenceNumberFactory;
    }

    @Override
    @Transactional
    public Dentist registerDentist(Dentist dentist) {
        normalizeDentist(dentist);
        dentist.setDentistCode(generateUniqueDentistCode());
        dentist.setActive(true);

        return dentistRepository.save(dentist);
    }

    @Override
    @Transactional
    public Dentist updateDentist(Long dentistId, Dentist updatedDentist) {
        Dentist existingDentist = getDentistById(dentistId);

        existingDentist.setFullName(updatedDentist.getFullName());
        existingDentist.setSpecialization(updatedDentist.getSpecialization());
        existingDentist.setContactNumber(updatedDentist.getContactNumber());
        existingDentist.setEmail(updatedDentist.getEmail());

        normalizeDentist(existingDentist);

        return dentistRepository.save(existingDentist);
    }

    @Override
    @Transactional(readOnly = true)
    public Dentist getDentistById(Long dentistId) {
        return dentistRepository.findById(dentistId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dentist", "ID", dentistId));
    }

    @Override
    @Transactional(readOnly = true)
    public Dentist getDentistByCode(String dentistCode) {
        String normalizedCode = dentistCode == null
                ? ""
                : dentistCode.trim().toUpperCase(Locale.ROOT);

        return dentistRepository.findByDentistCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dentist", "dentist code", normalizedCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dentist> searchDentists(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return getActiveDentists();
        }

        return dentistRepository
                .findByFullNameContainingIgnoreCaseOrderByFullNameAsc(
                        searchText.trim())
                .stream()
                .filter(Dentist::isActive)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dentist> getActiveDentists() {
        return dentistRepository.findByActiveTrueOrderByFullNameAsc();
    }

    @Override
    @Transactional
    public void deactivateDentist(Long dentistId) {
        Dentist dentist = getDentistById(dentistId);
        dentist.setActive(false);
        dentistRepository.save(dentist);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveDentists() {
        return dentistRepository.countByActiveTrue();
    }

    private String generateUniqueDentistCode() {
        for (int attempt = 0;
                attempt < MAX_REFERENCE_ATTEMPTS;
                attempt++) {

            String candidate =
                    referenceNumberFactory.create(ReferenceType.DENTIST);

            if (!dentistRepository.existsByDentistCode(candidate)) {
                return candidate;
            }
        }

        throw new BusinessRuleException(
                "Unable to generate a unique dentist code");
    }

    private void normalizeDentist(Dentist dentist) {
        dentist.setFullName(trim(dentist.getFullName()));
        dentist.setSpecialization(trim(dentist.getSpecialization()));
        dentist.setContactNumber(trim(dentist.getContactNumber()));

        if (dentist.getEmail() == null ||
                dentist.getEmail().isBlank()) {

            dentist.setEmail(null);
        } else {
            dentist.setEmail(
                    dentist.getEmail()
                            .trim()
                            .toLowerCase(Locale.ROOT));
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}