package com.journeyplus.trip.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "visa_requirements")
public class VisaRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_request_id", nullable = false)
    private TripRequest tripRequest;

    @Column(name = "destination_country", nullable = false, length = 100)
    private String destinationCountry;

    @Column(name = "requires_visa")
    private boolean requiresVisa = false;

    @Column(nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, APPLIED, APPROVED, EXEMPTED

    @Column(columnDefinition = "TEXT")
    private String notes;

    public VisaRequirement() {}

    public VisaRequirement(TripRequest tripRequest, String destinationCountry, boolean requiresVisa, String status, String notes) {
        this.tripRequest = tripRequest;
        this.destinationCountry = destinationCountry;
        this.requiresVisa = requiresVisa;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TripRequest getTripRequest() {
        return tripRequest;
    }

    public void setTripRequest(TripRequest tripRequest) {
        this.tripRequest = tripRequest;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public boolean isRequiresVisa() {
        return requiresVisa;
    }

    public void setRequiresVisa(boolean requiresVisa) {
        this.requiresVisa = requiresVisa;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
