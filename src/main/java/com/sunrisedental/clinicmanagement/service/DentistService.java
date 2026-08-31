package com.sunrisedental.clinicmanagement.service;

import java.util.List;

import com.sunrisedental.clinicmanagement.model.Dentist;

public interface DentistService {

    Dentist registerDentist(Dentist dentist);

    Dentist updateDentist(Long dentistId, Dentist updatedDentist);

    Dentist getDentistById(Long dentistId);

    Dentist getDentistByCode(String dentistCode);

    List<Dentist> searchDentists(String searchText);

    List<Dentist> getActiveDentists();

    void deactivateDentist(Long dentistId);

    long countActiveDentists();
}