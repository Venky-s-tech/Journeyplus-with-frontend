package com.journeyplus.advance.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.journeyplus.common.EncryptedBigDecimalConverter;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.entity.TripRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "advance_requests")
public class AdvanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_request_id", nullable = false)
    private TripRequest tripRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(nullable = false, length = 255)
    private BigDecimal amount;

    @Column(name = "original_currency", nullable = false, length = 10)
    private String originalCurrency;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "usd_equivalent", nullable = false, length = 255)
    private BigDecimal usdEquivalent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private AdvanceStatus status = AdvanceStatus.REQUESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate = LocalDate.now();

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    public AdvanceRequest() {}

    public AdvanceRequest(TripRequest tripRequest, User employee, BigDecimal amount, String originalCurrency, BigDecimal usdEquivalent) {
        this.tripRequest = tripRequest;
        this.employee = employee;
        this.amount = amount;
        this.originalCurrency = originalCurrency;
        this.usdEquivalent = usdEquivalent;
        this.status = AdvanceStatus.REQUESTED;
        this.requestDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public TripRequest getTripRequest() {
        return tripRequest;
    }

    @JsonProperty("tripRequestId")
    public Long getTripRequestId() {
        return tripRequest != null ? tripRequest.getId() : null;
    }

    public void setTripRequest(TripRequest tripRequest) {
        this.tripRequest = tripRequest;
    }

    @JsonIgnore
    public User getEmployee() {
        return employee;
    }

    @JsonProperty("employeeId")
    public Long getEmployeeId() {
        return employee != null ? employee.getId() : null;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public void setOriginalCurrency(String originalCurrency) {
        this.originalCurrency = originalCurrency;
    }

    public BigDecimal getUsdEquivalent() {
        return usdEquivalent;
    }

    public void setUsdEquivalent(BigDecimal usdEquivalent) {
        this.usdEquivalent = usdEquivalent;
    }

    public AdvanceStatus getStatus() {
        return status;
    }

    public void setStatus(AdvanceStatus status) {
        this.status = status;
    }

    @JsonIgnore
    public User getApprover() {
        return approver;
    }

    @JsonProperty("approverId")
    public Long getApproverId() {
        return approver != null ? approver.getId() : null;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getDisbursementDate() {
        return disbursementDate;
    }

    public void setDisbursementDate(LocalDate disbursementDate) {
        this.disbursementDate = disbursementDate;
    }
}
