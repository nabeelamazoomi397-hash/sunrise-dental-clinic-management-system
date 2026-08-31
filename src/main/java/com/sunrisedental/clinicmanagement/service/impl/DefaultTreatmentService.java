package com.sunrisedental.clinicmanagement.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunrisedental.clinicmanagement.exception.BusinessRuleException;
import com.sunrisedental.clinicmanagement.exception.ResourceNotFoundException;
import com.sunrisedental.clinicmanagement.model.Treatment;
import com.sunrisedental.clinicmanagement.model.enums.ReferenceType;
import com.sunrisedental.clinicmanagement.repository.TreatmentRepository;
import com.sunrisedental.clinicmanagement.service.TreatmentService;
import com.sunrisedental.clinicmanagement.util.ReferenceNumberFactory;

@Service
public class DefaultTreatmentService implements TreatmentService {

    private static final int MAX_REFERENCE_ATTEMPTS = 10;

    private final TreatmentRepository treatmentRepository;
    private final ReferenceNumberFactory referenceNumberFactory;

    public DefaultTreatmentService(
            TreatmentRepository treatmentRepository,
            ReferenceNumberFactory referenceNumberFactory) {

        this.treatmentRepository = treatmentRepository;
        this.referenceNumberFactory = referenceNumberFactory;
    }

    @Override
    @Transactional
    public Treatment createTreatment(Treatment treatment) {
        normalizeTreatment(treatment);
        validateTreatment(treatment);
        validateUniqueName(treatment.getName(), null);

        treatment.setTreatmentCode(generateUniqueTreatmentCode());
        treatment.setActive(true);

        return treatmentRepository.save(treatment);
    }

    @Override
    @Transactional
    public Treatment updateTreatment(
            Long treatmentId,
            Treatment updatedTreatment) {

        Treatment existingTreatment =
                getTreatmentById(treatmentId);

        existingTreatment.setName(updatedTreatment.getName());
        existingTreatment.setDescription(
                updatedTreatment.getDescription());
        existingTreatment.setPrice(updatedTreatment.getPrice());
        existingTreatment.setDurationMinutes(
                updatedTreatment.getDurationMinutes());

        normalizeTreatment(existingTreatment);
        validateTreatment(existingTreatment);
        validateUniqueName(
                existingTreatment.getName(),
                existingTreatment.getId());

        return treatmentRepository.save(existingTreatment);
    }

    @Override
    @Transactional(readOnly = true)
    public Treatment getTreatmentById(Long treatmentId) {
        return treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Treatment", "ID", treatmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Treatment getTreatmentByCode(String treatmentCode) {
        String normalizedCode = treatmentCode == null
                ? ""
                : treatmentCode.trim().toUpperCase(Locale.ROOT);

        return treatmentRepository
                .findByTreatmentCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Treatment",
                        "treatment code",
                        normalizedCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Treatment> searchTreatments(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return getActiveTreatments();
        }

        return treatmentRepository
                .findByNameContainingIgnoreCaseOrderByNameAsc(
                        searchText.trim())
                .stream()
                .filter(Treatment::isActive)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Treatment> getActiveTreatments() {
        return treatmentRepository
                .findByActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional
    public void deactivateTreatment(Long treatmentId) {
        Treatment treatment = getTreatmentById(treatmentId);
        treatment.setActive(false);
        treatmentRepository.save(treatment);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveTreatments() {
        return treatmentRepository.countByActiveTrue();
    }

    private String generateUniqueTreatmentCode() {
        for (int attempt = 0;
                attempt < MAX_REFERENCE_ATTEMPTS;
                attempt++) {

            String candidate =
                    referenceNumberFactory.create(
                            ReferenceType.TREATMENT);

            if (!treatmentRepository
                    .existsByTreatmentCode(candidate)) {

                return candidate;
            }
        }

        throw new BusinessRuleException(
                "Unable to generate a unique treatment code");
    }

    private void validateUniqueName(
            String treatmentName,
            Long currentTreatmentId) {

        treatmentRepository
                .findByNameIgnoreCase(treatmentName)
                .ifPresent(existingTreatment -> {

                    boolean belongsToAnotherTreatment =
                            currentTreatmentId == null ||
                            !existingTreatment.getId()
                                    .equals(currentTreatmentId);

                    if (belongsToAnotherTreatment) {
                        throw new BusinessRuleException(
                                "A treatment with this name already exists");
                    }
                });
    }

    private void validateTreatment(Treatment treatment) {
        if (treatment.getPrice() == null ||
                treatment.getPrice()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new BusinessRuleException(
                    "Treatment price cannot be negative");
        }

        if (treatment.getDurationMinutes() < 15 ||
                treatment.getDurationMinutes() > 480) {

            throw new BusinessRuleException(
                    "Treatment duration must be between 15 and 480 minutes");
        }
    }

    private void normalizeTreatment(Treatment treatment) {
        treatment.setName(trim(treatment.getName()));
        treatment.setDescription(
                trimToNull(treatment.getDescription()));
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