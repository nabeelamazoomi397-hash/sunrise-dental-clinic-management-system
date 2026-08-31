package com.sunrisedental.clinicmanagement.service.impl;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunrisedental.clinicmanagement.exception.BusinessRuleException;
import com.sunrisedental.clinicmanagement.exception.ResourceNotFoundException;
import com.sunrisedental.clinicmanagement.model.Patient;
import com.sunrisedental.clinicmanagement.model.enums.ReferenceType;
import com.sunrisedental.clinicmanagement.repository.PatientRepository;
import com.sunrisedental.clinicmanagement.service.PatientService;
import com.sunrisedental.clinicmanagement.util.ReferenceNumberFactory;

/**
 * Default implementation of patient-management business rules.
 */
@Service
public class DefaultPatientService implements PatientService {

    private static final int MAX_REFERENCE_ATTEMPTS = 10;

    private final PatientRepository patientRepository;
    private final ReferenceNumberFactory referenceNumberFactory;

    public DefaultPatientService(
            PatientRepository patientRepository,
            ReferenceNumberFactory referenceNumberFactory) {
        this.patientRepository = patientRepository;
        this.referenceNumberFactory = referenceNumberFactory;
    }

    @Override
    @Transactional
    public Patient registerPatient(Patient patient) {
        normalizePatient(patient);
        patient.setPatientNumber(generateUniquePatientNumber());
        patient.setActive(true);

        return patientRepository.save(patient);
    }

    @Override
    @Transactional
    public Patient updatePatient(
            Long patientId,
            Patient updatedPatient) {
        Patient existingPatient = getPatientById(patientId);

        existingPatient.setFullName(updatedPatient.getFullName());
        existingPatient.setAddress(updatedPatient.getAddress());
        existingPatient.setContactNumber(updatedPatient.getContactNumber());
        existingPatient.setEmail(updatedPatient.getEmail());
        existingPatient.setDateOfBirth(updatedPatient.getDateOfBirth());
        existingPatient.setGender(updatedPatient.getGender());
        existingPatient.setMedicalNotes(updatedPatient.getMedicalNotes());

        normalizePatient(existingPatient);

        return patientRepository.save(existingPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public Patient getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient",
                        "ID",
                        patientId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Patient getPatientByNumber(String patientNumber) {
        String normalizedNumber = patientNumber == null
                ? ""
                : patientNumber.trim().toUpperCase(Locale.ROOT);

        return patientRepository.findByPatientNumber(normalizedNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient",
                        "patient number",
                        normalizedNumber
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> searchPatients(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return getActivePatients();
        }

        return patientRepository
                .findByFullNameContainingIgnoreCaseOrderByFullNameAsc(
                        searchText.trim()
                )
                .stream()
                .filter(Patient::isActive)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> getActivePatients() {
        return patientRepository.findByActiveTrueOrderByFullNameAsc();
    }

    @Override
    @Transactional
    public void deactivatePatient(Long patientId) {
        Patient patient = getPatientById(patientId);
        patient.setActive(false);
        patientRepository.save(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActivePatients() {
        return patientRepository.countByActiveTrue();
    }

    private String generateUniquePatientNumber() {
        for (int attempt = 0;
                attempt < MAX_REFERENCE_ATTEMPTS;
                attempt++) {
            String candidate =
                    referenceNumberFactory.create(ReferenceType.PATIENT);

            if (!patientRepository.existsByPatientNumber(candidate)) {
                return candidate;
            }
        }

        throw new BusinessRuleException(
                "Unable to generate a unique patient number"
        );
    }

    private void normalizePatient(Patient patient) {
        patient.setFullName(trim(patient.getFullName()));
        patient.setAddress(trim(patient.getAddress()));
        patient.setContactNumber(trim(patient.getContactNumber()));
        patient.setMedicalNotes(trimToNull(patient.getMedicalNotes()));

        if (patient.getEmail() == null || patient.getEmail().isBlank()) {
            patient.setEmail(null);
        } else {
            patient.setEmail(
                    patient.getEmail()
                            .trim()
                            .toLowerCase(Locale.ROOT)
            );
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}