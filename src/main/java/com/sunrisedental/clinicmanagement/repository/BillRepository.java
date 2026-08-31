package com.sunrisedental.clinicmanagement.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sunrisedental.clinicmanagement.model.Bill;
import com.sunrisedental.clinicmanagement.model.enums.PaymentStatus;

/**
 * Provides billing searches, payment tracking and revenue calculations.
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    @EntityGraph(attributePaths = {
        "appointment",
        "appointment.patient",
        "appointment.dentist",
        "appointment.treatment"
    })
    Optional<Bill> findByBillNumber(String billNumber);

    @EntityGraph(attributePaths = {
        "appointment",
        "appointment.patient",
        "appointment.dentist",
        "appointment.treatment"
    })
    Optional<Bill> findByAppointmentId(Long appointmentId);

    boolean existsByBillNumber(String billNumber);

    boolean existsByAppointmentId(Long appointmentId);

    @EntityGraph(attributePaths = {
        "appointment",
        "appointment.patient",
        "appointment.dentist",
        "appointment.treatment"
    })
    List<Bill> findByPaymentStatusOrderByIssuedAtDesc(
            PaymentStatus paymentStatus);

    @EntityGraph(attributePaths = {
        "appointment",
        "appointment.patient",
        "appointment.dentist",
        "appointment.treatment"
    })
    List<Bill> findByIssuedAtBetweenOrderByIssuedAtDesc(
            LocalDateTime start,
            LocalDateTime end);

    @Query("""
        SELECT COALESCE(SUM(b.amountPaid), 0)
        FROM Bill b
        WHERE b.issuedAt BETWEEN :start AND :end
          AND b.paymentStatus <> com.sunrisedental.clinicmanagement.model.enums.PaymentStatus.REFUNDED
        """)
    BigDecimal calculateRevenueBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}