package com.journeyplus.trip.entity;

import com.journeyplus.trip.dto.VisaStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "visa_requirements")
@Getter
@Setter
public class VisaRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Trip request is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_request_id", nullable = false)
    private TripRequest tripRequest;

    @NotBlank(message = "Country is required")
    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @NotBlank(message = "Visa type is required")
    @Column(name = "visa_type", nullable = false, length = 100)
    private String visaType;

    @Column(name = "requires_visa")
    private boolean requiresVisa = false;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "submitted_date")
    private LocalDate submittedDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private VisaStatus status = VisaStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

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

    public VisaRequirement() {}

    public VisaRequirement(TripRequest tripRequest, String country, String visaType, boolean requiresVisa, VisaStatus status, String notes) {
        this.tripRequest = tripRequest;
        this.country = country;
        this.visaType = visaType;
        this.requiresVisa = requiresVisa;
        this.status = status;
        this.notes = notes;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Deprecated getters and setters for backward compatibility
    @Deprecated
    public String getDestinationCountry() { return country; }
    @Deprecated
    public void setDestinationCountry(String destinationCountry) { this.country = destinationCountry; }
}
