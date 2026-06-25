package com.journeyplus.trip.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class VisaRequirementInput {

    @NotBlank(message = "Country is required")
    @JsonAlias("destinationCountry")
    private String country;

    @NotBlank(message = "Visa type is required")
    private String visaType;

    private boolean requiresVisa;
    private LocalDate applicationDate;
    private LocalDate submittedDate;

    @NotNull(message = "Visa status is required")
    private VisaStatus status = VisaStatus.PENDING;

    private String notes;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getVisaType() {
        return visaType;
    }

    public void setVisaType(String visaType) {
        this.visaType = visaType;
    }

    public boolean isRequiresVisa() {
        return requiresVisa;
    }

    public void setRequiresVisa(boolean requiresVisa) {
        this.requiresVisa = requiresVisa;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDate getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDate submittedDate) {
        this.submittedDate = submittedDate;
    }

    public VisaStatus getStatus() {
        return status;
    }

    public void setStatus(VisaStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Deprecated getters and setters for backward compatibility
    @Deprecated
    public String getDestinationCountry() { return country; }
    @Deprecated
    public void setDestinationCountry(String destinationCountry) { this.country = destinationCountry; }
}
