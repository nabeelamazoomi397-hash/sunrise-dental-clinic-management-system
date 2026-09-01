package com.sunrisedental.clinicmanagement.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.sunrisedental.clinicmanagement.model.Bill;
import com.sunrisedental.clinicmanagement.model.enums.PaymentMethod;

public interface BillingService {

    Bill generateBill(
            Long appointmentId,
            BigDecimal consultationFee,
            BigDecimal discount);

    Bill getBillById(Long billId);

    Bill getBillByNumber(String billNumber);

    Bill getBillForAppointment(Long appointmentId);

    Bill recordPayment(
            Long billId,
            BigDecimal paymentAmount,
            PaymentMethod paymentMethod);

    List<Bill> getBillsBetween(
            LocalDate startDate,
            LocalDate endDate);

    BigDecimal calculateRevenueBetween(
            LocalDate startDate,
            LocalDate endDate);
}