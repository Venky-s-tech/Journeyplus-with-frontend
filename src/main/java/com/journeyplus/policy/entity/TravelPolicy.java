package com.journeyplus.policy.entity;

import com.journeyplus.common.EncryptedBigDecimalConverter;
import com.journeyplus.iam.entity.Role;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "travel_policies")
public class TravelPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_name", nullable = false, length = 150)
    private String policyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_role", nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    private Role employeeRole;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "max_amount_per_trip", nullable = false, length = 255)
    private BigDecimal maxAmountPerTrip;

    @Column(name = "requires_visa_verification")
    private boolean requiresVisaVerification = false;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public TravelPolicy() {}

    public TravelPolicy(String policyName, String description, Role employeeRole, BigDecimal maxAmountPerTrip, boolean requiresVisaVerification, String createdBy) {
        this.policyName = policyName;
        this.description = description;
        this.employeeRole = employeeRole;
        this.maxAmountPerTrip = maxAmountPerTrip;
        this.requiresVisaVerification = requiresVisaVerification;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Role getEmployeeRole() {
        return employeeRole;
    }

    public void setEmployeeRole(Role employeeRole) {
        this.employeeRole = employeeRole;
    }

    public BigDecimal getMaxAmountPerTrip() {
        return maxAmountPerTrip;
    }

    public void setMaxAmountPerTrip(BigDecimal maxAmountPerTrip) {
        this.maxAmountPerTrip = maxAmountPerTrip;
    }

    public boolean isRequiresVisaVerification() {
        return requiresVisaVerification;
    }

    public void setRequiresVisaVerification(boolean requiresVisaVerification) {
        this.requiresVisaVerification = requiresVisaVerification;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
