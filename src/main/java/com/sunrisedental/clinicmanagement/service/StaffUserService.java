package com.sunrisedental.clinicmanagement.service;

import java.util.List;

import com.sunrisedental.clinicmanagement.model.StaffUser;
import com.sunrisedental.clinicmanagement.model.enums.Role;

public interface StaffUserService {

    StaffUser createStaffUser(
            StaffUser staffUser,
            String rawPassword);

    StaffUser updateStaffUser(
            Long staffUserId,
            StaffUser updatedStaffUser);

    StaffUser getStaffUserById(Long staffUserId);

    StaffUser getStaffUserByUsername(String username);

    List<StaffUser> getActiveStaffUsers();

    List<StaffUser> getActiveStaffUsersByRole(Role role);

    StaffUser unlockStaffUser(Long staffUserId);

    void deactivateStaffUser(Long staffUserId);

    long countActiveStaffUsers();
}