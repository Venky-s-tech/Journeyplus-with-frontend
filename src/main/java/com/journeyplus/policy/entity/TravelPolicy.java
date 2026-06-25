package com.journeyplus.policy.entity;

import com.journeyplus.common.EncryptedBigDecimalConverter;
import com.journeyplus.iam.entity.Grade;
import com.journeyplus.iam.entity.Role;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "travel_policies", indexes = {
    @Index(name = "idx_travel_policies_grade_type", columnList = "grade_id, travel_type"),
    @Index(name = "idx_travel_policies_status", columnList = "status")
})
@Getter
@Setter
public class TravelPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_name", nullable = false, length = 150)
    private String policyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_type", nullable = false, length = 50)
    private TravelType travelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "flight_class", nullable = false, length = 50)
    private FlightClass flightClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "hotel_category", nullable = false, length = 50)
    private HotelCategory hotelCategory;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "per_diem_rate", nullable = false, length = 255)
    private BigDecimal perDiemRate;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "local_conveyance_limit", nullable = false, length = 255)
    private BigDecimal localConveyanceLimit;

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PolicyStatus status = PolicyStatus.ACTIVE;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate = LocalDateTime.now();

    // ==========================================
    // Backward Compatibility Fields for Compliance Module
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_role", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private Role employeeRole = Role.EMPLOYEE;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "max_amount_per_trip", nullable = false, length = 255)
    private BigDecimal maxAmountPerTrip = new BigDecimal("999999.00");

    @Column(name = "requires_visa_verification")
    private boolean requiresVisaVerification = false;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy = "SYSTEM";

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        syncEmployeeRole();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
        syncEmployeeRole();
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
        syncEmployeeRole();
    }

    private void syncEmployeeRole() {
        if (this.grade != null) {
            String gid = this.grade.getId();
            if ("G1".equalsIgnoreCase(gid) || "G2".equalsIgnoreCase(gid)) {
                this.employeeRole = Role.EMPLOYEE;
            } else if ("G3".equalsIgnoreCase(gid) || "G4".equalsIgnoreCase(gid)) {
                this.employeeRole = Role.APPROVING_MANAGER;
            } else if ("G5".equalsIgnoreCase(gid)) {
                // Return a valid Role like COMPLIANCE
                this.employeeRole = Role.COMPLIANCE;
            } else if ("G6".equalsIgnoreCase(gid)) {
                this.employeeRole = Role.ADMIN;
            } else {
                this.employeeRole = Role.EMPLOYEE;
            }
        }
    }

    // Compatibility getters
    public LocalDateTime getCreatedAt() {
        return createdDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedDate;
    }

    public TravelPolicy() {}

    public TravelPolicy(String policyName, String description, Grade grade, TravelType travelType, 
                        FlightClass flightClass, HotelCategory hotelCategory, BigDecimal perDiemRate, 
                        BigDecimal localConveyanceLimit, LocalDateTime effectiveDate, PolicyStatus status, String createdBy) {
        this.policyName = policyName;
        this.description = description;
        setGrade(grade);
        this.travelType = travelType;
        this.flightClass = flightClass;
        this.hotelCategory = hotelCategory;
        this.perDiemRate = perDiemRate;
        this.localConveyanceLimit = localConveyanceLimit;
        this.effectiveDate = effectiveDate != null ? effectiveDate : LocalDateTime.now();
        this.status = status != null ? status : PolicyStatus.ACTIVE;
        this.createdBy = createdBy != null ? createdBy : "SYSTEM";
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Constructor for backward compatibility in tests
    public TravelPolicy(String policyName, String description, Role employeeRole, BigDecimal maxAmountPerTrip, boolean requiresVisaVerification, String createdBy) {
        this.policyName = policyName;
        this.description = description;
        this.employeeRole = employeeRole;
        this.maxAmountPerTrip = maxAmountPerTrip;
        this.requiresVisaVerification = requiresVisaVerification;
        this.createdBy = createdBy;
        this.travelType = TravelType.DOMESTIC;
        this.flightClass = FlightClass.ECONOMY;
        this.hotelCategory = HotelCategory.STANDARD;
        this.perDiemRate = new BigDecimal("100.00");
        this.localConveyanceLimit = new BigDecimal("50.00");
        this.effectiveDate = LocalDateTime.now();
        this.status = PolicyStatus.ACTIVE;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
}
