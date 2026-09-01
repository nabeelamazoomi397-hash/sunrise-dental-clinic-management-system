package com.sunrisedental.clinicmanagement.service;

import java.util.List;

import com.sunrisedental.clinicmanagement.model.Treatment;

public interface TreatmentService {

    Treatment createTreatment(Treatment treatment);

    Treatment updateTreatment(
            Long treatmentId,
            Treatment updatedTreatment);

    Treatment getTreatmentById(Long treatmentId);

    Treatment getTreatmentByCode(String treatmentCode);

    List<Treatment> searchTreatments(String searchText);

    List<Treatment> getActiveTreatments();

    void deactivateTreatment(Long treatmentId);

    long countActiveTreatments();
}