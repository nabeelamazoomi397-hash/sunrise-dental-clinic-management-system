package com.sunrisedental.clinicmanagement.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.sunrisedental.clinicmanagement.model.StaffUser;
import com.sunrisedental.clinicmanagement.model.enums.Role;
import com.sunrisedental.clinicmanagement.repository.StaffUserRepository;
import com.sunrisedental.clinicmanagement.service.StaffUserService;

@Component
public class AdminAccountInitializer
        implements ApplicationRunner {

    private final StaffUserRepository staffUserRepository;
    private final StaffUserService staffUserService;

    @Value("${APP_ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${APP_ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${APP_ADMIN_FULL_NAME:System Administrator}")
    private String adminFullName;

    @Value("${APP_ADMIN_EMAIL:}")
    private String adminEmail;

    public AdminAccountInitializer(
            StaffUserRepository staffUserRepository,
            StaffUserService staffUserService) {

        this.staffUserRepository = staffUserRepository;
        this.staffUserService = staffUserService;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (staffUserRepository.count() > 0) {
            return;
        }

        if (adminUsername == null ||
                adminUsername.isBlank()) {

            throw new IllegalStateException(
                    "APP_ADMIN_USERNAME is required "
                    + "when creating the first administrator");
        }

        if (adminPassword == null ||
                adminPassword.isBlank()) {

            throw new IllegalStateException(
                    "APP_ADMIN_PASSWORD is required "
                    + "when creating the first administrator");
        }

        String safeEmail =
                adminEmail == null ||
                adminEmail.isBlank()
                        ? null
                        : adminEmail.trim();

        StaffUser administrator = new StaffUser(
                adminFullName,
                adminUsername,
                "PENDING_PASSWORD_ENCODING",
                safeEmail,
                Role.ADMIN);

        staffUserService.createStaffUser(
                administrator,
                adminPassword);
    }
}