package com.sunrisedental.clinicmanagement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunrisedental.clinicmanagement.exception.BusinessRuleException;
import com.sunrisedental.clinicmanagement.exception.ResourceNotFoundException;
import com.sunrisedental.clinicmanagement.model.Appointment;
import com.sunrisedental.clinicmanagement.model.Bill;
import com.sunrisedental.clinicmanagement.model.enums.AppointmentStatus;
import com.sunrisedental.clinicmanagement.model.enums.PaymentMethod;
import com.sunrisedental.clinicmanagement.model.enums.PaymentStatus;
import com.sunrisedental.clinicmanagement.model.enums.ReferenceType;
import com.sunrisedental.clinicmanagement.repository.AppointmentRepository;
import com.sunrisedental.clinicmanagement.repository.BillRepository;
import com.sunrisedental.clinicmanagement.service.BillingService;
import com.sunrisedental.clinicmanagement.util.ReferenceNumberFactory;

@Service
public class DefaultBillingService implements BillingService {

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");

    private static final int MAX_REFERENCE_ATTEMPTS = 10;

    private final BillRepository billRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReferenceNumberFactory referenceNumberFactory;

    public DefaultBillingService(
            BillRepository billRepository,
            AppointmentRepository appointmentRepository,
            ReferenceNumberFactory referenceNumberFactory) {

        this.billRepository = billRepository;
        this.appointmentRepository = appointmentRepository;
        this.referenceNumberFactory = referenceNumberFactory;
    }

    @Override
    @Transactional
    public Bill generateBill(
            Long appointmentId,
            BigDecimal consultationFee,
            BigDecimal discount) {

        Appointment appointment =
                appointmentRepository.findById(appointmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Appointment",
                                        "ID",
                                        appointmentId));

        if (billRepository.existsByAppointmentId(appointmentId)) {
            throw new BusinessRuleException(
                    "A bill has already been generated "
                    + "for this appointment");
        }

        if (appointment.getStatus() !=
                AppointmentStatus.COMPLETED) {

            throw new BusinessRuleException(
                    "A bill can only be generated "
                    + "after the appointment is completed");
        }

        BigDecimal treatmentCharge =
                appointment.getTreatment().getPrice();

        BigDecimal safeConsultationFee =
                validateNonNegativeAmount(
                        consultationFee,
                        "Consultation fee");

        BigDecimal safeDiscount =
                validateNonNegativeAmount(
                        discount,
                        "Discount");

        BigDecimal subtotal =
                treatmentCharge.add(safeConsultationFee);

        if (safeDiscount.compareTo(subtotal) > 0) {
            throw new BusinessRuleException(
                    "Discount cannot be greater "
                    + "than the bill subtotal");
        }

        Bill bill = new Bill(
                generateUniqueBillNumber(),
                appointment,
                treatmentCharge,
                safeConsultationFee,
                safeDiscount);

        bill.setAmountPaid(ZERO);
        bill.setPaymentStatus(PaymentStatus.PENDING);

        return billRepository.save(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public Bill getBillById(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill", "ID", billId));
    }

    @Override
    @Transactional(readOnly = true)
    public Bill getBillByNumber(String billNumber) {
        String normalizedNumber =
                billNumber == null
                        ? ""
                        : billNumber.trim()
                                .toUpperCase(Locale.ROOT);

        return billRepository
                .findByBillNumber(normalizedNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill",
                        "bill number",
                        normalizedNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Bill getBillForAppointment(Long appointmentId) {
        return billRepository
                .findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill",
                        "appointment ID",
                        appointmentId));
    }

    @Override
    @Transactional
    public Bill recordPayment(
            Long billId,
            BigDecimal paymentAmount,
            PaymentMethod paymentMethod) {

        Bill bill = getBillById(billId);

        if (bill.getPaymentStatus() ==
                PaymentStatus.REFUNDED) {

            throw new BusinessRuleException(
                    "Payment cannot be recorded "
                    + "for a refunded bill");
        }

        if (bill.getPaymentStatus() ==
                PaymentStatus.PAID) {

            throw new BusinessRuleException(
                    "This bill has already been paid");
        }

        if (paymentMethod == null) {
            throw new BusinessRuleException(
                    "Payment method is required");
        }

        if (paymentAmount == null ||
                paymentAmount.compareTo(ZERO) <= 0) {

            throw new BusinessRuleException(
                    "Payment amount must be greater than zero");
        }

        BigDecimal newAmountPaid =
                bill.getAmountPaid().add(paymentAmount);

        if (newAmountPaid.compareTo(
                bill.getTotalAmount()) > 0) {

            throw new BusinessRuleException(
                    "Payment cannot be greater "
                    + "than the remaining balance");
        }

        bill.setAmountPaid(newAmountPaid);
        bill.setPaymentMethod(paymentMethod);

        if (newAmountPaid.compareTo(
                bill.getTotalAmount()) == 0) {

            bill.setPaymentStatus(PaymentStatus.PAID);
            bill.setPaidAt(LocalDateTime.now());
        } else {
            bill.setPaymentStatus(
                    PaymentStatus.PARTIALLY_PAID);
            bill.setPaidAt(null);
        }

        return billRepository.save(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> getBillsBetween(
            LocalDate startDate,
            LocalDate endDate) {

        validateDateRange(startDate, endDate);

        LocalDateTime start =
                startDate.atStartOfDay();

        LocalDateTime end =
                endDate.atTime(LocalTime.MAX);

        return billRepository
                .findByIssuedAtBetweenOrderByIssuedAtDesc(
                        start,
                        end);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueBetween(
            LocalDate startDate,
            LocalDate endDate) {

        validateDateRange(startDate, endDate);

        LocalDateTime start =
                startDate.atStartOfDay();

        LocalDateTime end =
                endDate.atTime(LocalTime.MAX);

        BigDecimal revenue =
                billRepository.calculateRevenueBetween(
                        start,
                        end);

        return revenue == null ? ZERO : revenue;
    }

    private BigDecimal validateNonNegativeAmount(
            BigDecimal amount,
            String fieldName) {

        BigDecimal safeAmount =
                amount == null ? ZERO : amount;

        if (safeAmount.compareTo(ZERO) < 0) {
            throw new BusinessRuleException(
                    fieldName + " cannot be negative");
        }

        return safeAmount;
    }

    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new BusinessRuleException(
                    "Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException(
                    "End date cannot be before start date");
        }
    }

    private String generateUniqueBillNumber() {
        for (int attempt = 0;
                attempt < MAX_REFERENCE_ATTEMPTS;
                attempt++) {

            String candidate =
                    referenceNumberFactory.create(
                            ReferenceType.BILL);

            if (!billRepository
                    .existsByBillNumber(candidate)) {

                return candidate;
            }
        }

        throw new BusinessRuleException(
                "Unable to generate a unique bill number");
    }
}