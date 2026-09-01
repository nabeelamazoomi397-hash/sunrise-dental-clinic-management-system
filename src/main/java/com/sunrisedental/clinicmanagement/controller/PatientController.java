package com.sunrisedental.clinicmanagement.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sunrisedental.clinicmanagement.exception.BusinessRuleException;
import com.sunrisedental.clinicmanagement.exception.DuplicateResourceException;
import com.sunrisedental.clinicmanagement.model.Patient;
import com.sunrisedental.clinicmanagement.model.enums.Gender;
import com.sunrisedental.clinicmanagement.service.PatientService;

import jakarta.validation.Valid;

/**
 * Handles patient registration, searching, editing and deactivation pages.
 */
@Controller
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Displays active patients or patients matching the search text.
     */
    @GetMapping
    public String listPatients(
            @RequestParam(name = "search", required = false)
            String searchText,
            Model model) {

        List<Patient> patients;

        if (searchText == null || searchText.isBlank()) {
            patients = patientService.getActivePatients();
        } else {
            patients = patientService.searchPatients(searchText);
        }

        model.addAttribute("patients", patients);
        model.addAttribute(
                "searchText",
                searchText == null ? "" : searchText);
        model.addAttribute(
                "patientCount",
                patientService.countActivePatients());

        return "patients/list";
    }

    /**
     * Displays the patient-registration form.
     */
    @GetMapping("/new")
    public String showRegistrationForm(Model model) {

        model.addAttribute("patient", new Patient());
        addFormAttributes(model, false);

        return "patients/form";
    }

    /**
     * Validates and registers a new patient.
     */
    @PostMapping
    public String registerPatient(
            @Valid @ModelAttribute("patient") Patient patient,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            addFormAttributes(model, false);
            return "patients/form";
        }

        try {
            Patient savedPatient =
                    patientService.registerPatient(patient);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Patient "
                            + savedPatient.getPatientNumber()
                            + " was registered successfully.");

            return "redirect:/patients";

        } catch (DuplicateResourceException exception) {
            bindingResult.reject(
                    "patient.duplicate",
                    exception.getMessage());

        } catch (BusinessRuleException exception) {
            bindingResult.reject(
                    "patient.businessRule",
                    exception.getMessage());
        }

        addFormAttributes(model, false);
        return "patients/form";
    }

    /**
     * Displays one patient's complete information.
     */
    @GetMapping("/{patientId}")
    public String showPatientDetails(
            @PathVariable("patientId") Long patientId,
            Model model) {

        model.addAttribute(
                "patient",
                patientService.getPatientById(patientId));

        return "patients/details";
    }

    /**
     * Displays the form used to edit a patient.
     */
    @GetMapping("/{patientId}/edit")
    public String showEditForm(
            @PathVariable("patientId") Long patientId,
            Model model) {

        model.addAttribute(
                "patient",
                patientService.getPatientById(patientId));

        addFormAttributes(model, true);

        return "patients/form";
    }

    /**
     * Validates and updates an existing patient.
     */
    @PostMapping("/{patientId}/edit")
    public String updatePatient(
            @PathVariable("patientId") Long patientId,
            @Valid @ModelAttribute("patient") Patient patient,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            addFormAttributes(model, true);
            return "patients/form";
        }

        try {
            Patient updatedPatient =
                    patientService.updatePatient(
                            patientId,
                            patient);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Patient "
                            + updatedPatient.getPatientNumber()
                            + " was updated successfully.");

            return "redirect:/patients/" + patientId;

        } catch (DuplicateResourceException exception) {
            bindingResult.reject(
                    "patient.duplicate",
                    exception.getMessage());

        } catch (BusinessRuleException exception) {
            bindingResult.reject(
                    "patient.businessRule",
                    exception.getMessage());
        }

        addFormAttributes(model, true);
        return "patients/form";
    }

    /**
     * Deactivates a patient while preserving their database history.
     */
    @PostMapping("/{patientId}/deactivate")
    public String deactivatePatient(
            @PathVariable("patientId") Long patientId,
            RedirectAttributes redirectAttributes) {

        patientService.deactivatePatient(patientId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "The patient record was deactivated successfully.");

        return "redirect:/patients";
    }

    /**
     * Adds shared values required by registration and editing forms.
     */
    private void addFormAttributes(
            Model model,
            boolean editing) {

        model.addAttribute("genders", Gender.values());
        model.addAttribute("editing", editing);
        model.addAttribute(
                "maximumBirthDate",
                LocalDate.now().minusDays(1));
    }
}