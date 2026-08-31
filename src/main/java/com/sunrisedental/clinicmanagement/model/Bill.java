package com.sunrisedental.clinicmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sunrisedental.clinicmanagement.model.enums.PaymentMethod;
import com.sunrisedental.clinicmanagement.model.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Stores the billing details associated with one dental appointment.
 */
@Entity
@Table(
    name = "bills",
    indexes = {
        @Index(name = "idx_bill_number", columnList = "bill_number"),
        @Index(name = "idx_payment_status", columnList = "payment_status")
    }
)
public class Bill {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Bill number is required")
    @Column(name = "bill_number", nullable = false, unique = true, length = 30)
    private String billNumber;

    @NotNull(message = "Appointment is required")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @NotNull(message = "Treatment charge is required")
    @DecimalMin(value = "0.00", message = "Treatment charge cannot be negative")
    @Column(name = "treatment_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal treatmentCharge = ZERO;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.00", message = "Consultation fee cannot be negative")
    @Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee = ZERO;

    @NotNull(message = "Discount is required")
    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount = ZERO;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", message = "Total amount cannot be negative")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = ZERO;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.00", message = "Amount paid cannot be negative")
    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid = ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 25)
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 25)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Bill() {
    }

    public Bill(
            Appointment appointment,
            BigDecimal treatmentCharge,
            BigDecimal consultationFee,
            BigDecimal discount) {
        this.appointment = appointment;
        this.treatmentCharge = safeAmount(treatmentCharge);
        this.consultationFee = safeAmount(consultationFee);
        this.discount = safeAmount(discount);
        recalculateTotal();
    }

    public Bill(
            String billNumber,
            Appointment appointment,
            BigDecimal treatmentCharge,
            BigDecimal consultationFee,
            BigDecimal discount) {
        this(appointment, treatmentCharge, consultationFee, discount);
        this.billNumber = billNumber;
    }

    @PrePersist
    protected void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        issuedAt = now;
        updatedAt = now;

        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }

        amountPaid = safeAmount(amountPaid);
        recalculateTotal();
    }

    @PreUpdate
    protected void beforeUpdate() {
        updatedAt = LocalDateTime.now();
        amountPaid = safeAmount(amountPaid);
        recalculateTotal();
    }

    /**
     * Applies the billing formula required by the assessment.
     */
    public void recalculateTotal() {
        BigDecimal calculatedTotal = safeAmount(treatmentCharge)
                .add(safeAmount(consultationFee))
                .subtract(safeAmount(discount));

        totalAmount = calculatedTotal.max(ZERO);
    }

    /**
     * Returns the remaining balance without storing duplicated data.
     */
    public BigDecimal getBalanceDue() {
        return safeAmount(totalAmount)
                .subtract(safeAmount(amountPaid))
                .max(ZERO);
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? ZERO : amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public BigDecimal getTreatmentCharge() {
        return treatmentCharge;
    }

    public void setTreatmentCharge(BigDecimal treatmentCharge) {
        this.treatmentCharge = safeAmount(treatmentCharge);
        recalculateTotal();
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = safeAmount(consultationFee);
        recalculateTotal();
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = safeAmount(discount);
        recalculateTotal();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = safeAmount(totalAmount);
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = safeAmount(amountPaid);
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}