package com.journeyplus.advance.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.journeyplus.common.EncryptedBigDecimalConverter;
import com.journeyplus.iam.entity.User;
import com.journeyplus.trip.entity.TripRequest;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "advance_requests", indexes = {
    @Index(name = "idx_advance_trip", columnList = "trip_request_id"),
    @Index(name = "idx_advance_employee", columnList = "employee_id"),
    @Index(name = "idx_advance_status", columnList = "status")
})
@Getter
@Setter
public class AdvanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Trip request is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_request_id", nullable = false)
    @JsonIgnore
    private TripRequest tripRequest;

    @NotNull(message = "Employee is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private User employee;

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "0.01", message = "Requested amount must be positive")
    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "requested_amount", nullable = false, length = 255)
    private BigDecimal requestedAmount;

    @NotBlank(message = "Currency is required")
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @NotBlank(message = "Purpose details are required")
    @Column(name = "purpose_details", nullable = false, columnDefinition = "TEXT")
    private String purposeDetails;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "usd_equivalent", nullable = false, length = 255)
    private BigDecimal usdEquivalent;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private AdvanceStatus status = AdvanceStatus.REQUESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    @JsonIgnore
    private User approvedBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public AdvanceRequest() {}

    public AdvanceRequest(TripRequest tripRequest, User employee, BigDecimal requestedAmount, String currency, String purposeDetails, BigDecimal usdEquivalent) {
        this.tripRequest = tripRequest;
        this.employee = employee;
        this.requestedAmount = requestedAmount;
        this.currency = currency;
        this.purposeDetails = purposeDetails;
        this.usdEquivalent = usdEquivalent;
        this.status = AdvanceStatus.REQUESTED;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // JSON properties and Deprecated methods for backward compatibility
    @JsonProperty("tripRequestId")
    public Long getTripRequestId() {
        return tripRequest != null ? tripRequest.getId() : null;
    }

    @JsonProperty("employeeId")
    public Long getEmployeeId() {
        return employee != null ? employee.getId() : null;
    }

    @JsonProperty("approverId")
    public Long getApproverId() {
        return approvedBy != null ? approvedBy.getId() : null;
    }

    @Deprecated
    @JsonIgnore
    public BigDecimal getAmount() {
        return requestedAmount;
    }

    @Deprecated
    @JsonIgnore
    public void setAmount(BigDecimal amount) {
        this.requestedAmount = amount;
    }

    @Deprecated
    @JsonIgnore
    public String getOriginalCurrency() {
        return currency;
    }

    @Deprecated
    @JsonIgnore
    public void setOriginalCurrency(String originalCurrency) {
        this.currency = originalCurrency;
    }

    @Deprecated
    @JsonIgnore
    public User getApprover() {
        return approvedBy;
    }

    @Deprecated
    @JsonIgnore
    public void setApprover(User approver) {
        this.approvedBy = approver;
    }

    @Deprecated
    @JsonIgnore
    public LocalDate getRequestDate() {
        return createdDate != null ? createdDate.toLocalDate() : null;
    }

    @Deprecated
    @JsonIgnore
    public void setRequestDate(LocalDate requestDate) {
        if (requestDate != null) {
            this.createdDate = requestDate.atStartOfDay();
        }
    }
}
