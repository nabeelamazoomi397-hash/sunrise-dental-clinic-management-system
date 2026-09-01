package com.sunrisedental.clinicmanagement.security;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunrisedental.clinicmanagement.model.StaffUser;
import com.sunrisedental.clinicmanagement.repository.StaffUserRepository;

@Service
public class ClinicUserDetailsService
        implements UserDetailsService {

    private final StaffUserRepository staffUserRepository;

    public ClinicUserDetailsService(
            StaffUserRepository staffUserRepository) {

        this.staffUserRepository = staffUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        String normalizedUsername =
                username == null
                        ? ""
                        : username.trim()
                                .toLowerCase(Locale.ROOT);

        StaffUser staffUser =
                staffUserRepository
                        .findByUsernameIgnoreCase(
                                normalizedUsername)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Invalid username or password"));

        return User.withUsername(staffUser.getUsername())
                .password(staffUser.getPasswordHash())
                .roles(staffUser.getRole().name())
                .disabled(!staffUser.isActive())
                .accountLocked(
                        staffUser.isAccountLocked())
                .build();
    }
}