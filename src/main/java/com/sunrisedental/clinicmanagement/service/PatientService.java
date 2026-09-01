package com.sunrisedental.clinicmanagement.service;

import java.util.List;

import com.sunrisedental.clinicmanagement.model.Patient;

/**
 * Defines the patient-management operations available to the application.
 */
public interface PatientService {

    Patient registerPatient(Patient patient);

    Patient updatePatient(Long patientId, Patient updatedPatient);

    Patient getPatientById(Long patientId);

    Patient getPatientByNumber(String patientNumber);

    List<Patient> searchPatients(String searchText);

    List<Patient> getActivePatients();

    void deactivatePatient(Long patientId);

    long countActivePatients();
}