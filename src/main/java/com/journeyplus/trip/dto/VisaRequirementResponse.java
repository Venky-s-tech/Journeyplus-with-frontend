package com.journeyplus.trip.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VisaRequirementResponse {
    private Long id;
    private String country;
    private String visaType;
    private boolean requiresVisa;
    private LocalDate applicationDate;
    private LocalDate submittedDate;
    private String status;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getVisaType() { return visaType; }
    public void setVisaType(String visaType) { this.visaType = visaType; }
    public boolean isRequiresVisa() { return requiresVisa; }
    public void setRequiresVisa(boolean requiresVisa) { this.requiresVisa = requiresVisa; }
    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }
    public LocalDate getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(LocalDate submittedDate) { this.submittedDate = submittedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    // Deprecated getters and setters for backward compatibility
    @Deprecated
    public String getDestinationCountry() { return country; }
    @Deprecated
    public void setDestinationCountry(String destinationCountry) { this.country = destinationCountry; }
}
