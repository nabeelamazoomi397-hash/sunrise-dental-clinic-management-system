package com.sunrisedental.clinicmanagement.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sunrisedental.clinicmanagement.model.enums.ReferenceType;

/**
 * Factory responsible for generating unique, readable business references.
 */
@Component
public class ReferenceNumberFactory {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public String create(ReferenceType referenceType) {
        String prefix = switch (referenceType) {
            case PATIENT -> "PAT";
            case DENTIST -> "DEN";
            case TREATMENT -> "TRT";
            case APPOINTMENT -> "APT";
            case BILL -> "BIL";
            case STAFF -> "STF";
        };

        String datePart = LocalDate.now().format(DATE_FORMAT);
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return String.format(
                "%s-%s-%s",
                prefix,
                datePart,
                randomPart
        );
    }
}