package com.journeyplus.trip.entity;

import com.journeyplus.iam.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.journeyplus.common.EncryptedBigDecimalConverter;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trip_requests", indexes = {
    @Index(name = "idx_trip_employee", columnList = "employee_id"),
    @Index(name = "idx_trip_status", columnList = "status"),
    @Index(name = "idx_trip_travel_type", columnList = "travel_type"),
    @Index(name = "idx_trip_destination", columnList = "destination")
})
@Getter
@Setter
public class TripRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Employee is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @NotBlank(message = "Purpose is required")
    @Column(nullable = false)
    private String purpose;

    @NotBlank(message = "Destination is required")
    @Column(nullable = false, length = 150)
    private String destination;

    @NotNull(message = "Departure date is required")
    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @NotNull(message = "Return date is required")
    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @NotBlank(message = "Travel type is required")
    @Column(name = "travel_type", length = 50)
    private String travelType; // DOMESTIC / INTERNATIONAL

    @DecimalMin(value = "0.01", message = "Estimated cost must be positive")
    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "estimated_cost", length = 255)
    private BigDecimal estimatedCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private TripStatus status = TripStatus.DRAFT;

    @Column(name = "booking_status", length = 50)
    private String bookingStatus = "DRAFT";

    @Column(name = "workflow_stage", length = 50)
    private String workflowStage = "DRAFT";

    @Column(name = "travel_desk_status", length = 50)
    private String travelDeskStatus = "UNASSIGNED";

    @Column(columnDefinition = "TEXT")
    private String comments;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public TripRequest() {}

    public TripRequest(User employee, String purpose, String destination, LocalDate departureDate, LocalDate returnDate) {
        this.employee = employee;
        this.purpose = purpose;
        this.destination = destination;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.status = TripStatus.DRAFT;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Deprecated getters and setters for backward compatibility with other modules
    @Deprecated
    public User getApprovingManager() {
        return approver;
    }

    @Deprecated
    public void setApprovingManager(User approvingManager) {
        this.approver = approvingManager;
    }

    @Deprecated
    public LocalDate getStartDate() {
        return departureDate;
    }

    @Deprecated
    public void setStartDate(LocalDate startDate) {
        this.departureDate = startDate;
    }

    @Deprecated
    public LocalDate getEndDate() {
        return returnDate;
    }

    @Deprecated
    public void setEndDate(LocalDate endDate) {
        this.returnDate = endDate;
    }

    @Deprecated
    public LocalDateTime getCreatedAt() {
        return createdDate;
    }

    @Deprecated
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdDate = createdAt;
    }

    @Deprecated
    public LocalDateTime getUpdatedAt() {
        return updatedDate;
    }

    @Deprecated
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedDate = updatedAt;
    }
}
