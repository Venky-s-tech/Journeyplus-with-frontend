package com.journeyplus.trip.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Request payload to update or set visa requirement for a trip")
public class VisaRequest {

    @Schema(description = "Destination country (name or ISO code)", example = "India")
    @NotBlank
    @JsonAlias("destinationCountry")
    private String country;

    @Schema(description = "Visa type (e.g. tourist, business, work)", example = "Business")
    @NotBlank
    private String visaType;

    @Schema(description = "Whether the traveller requires a visa for the destination", example = "true")
    @NotNull
    private Boolean requiresVisa;

    @Schema(description = "Visa application/status (NOT_REQUIRED, PENDING, APPLIED, GRANTED, REJECTED)", example = "PENDING", implementation = VisaStatus.class)
    @NotNull
    private VisaStatus status;

    private LocalDate applicationDate;
    private LocalDate submittedDate;

    @Schema(description = "Optional free-text notes about the visa", example = "Appointment scheduled at embassy")
    @Size(max = 2000)
    private String notes;

    public VisaRequest() {
    }

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

    public Boolean getRequiresVisa() {
        return requiresVisa;
    }

    public void setRequiresVisa(Boolean requiresVisa) {
        this.requiresVisa = requiresVisa;
    }

    public VisaStatus getStatus() {
        return status;
    }

    public void setStatus(VisaStatus status) {
        this.status = status;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Deprecated getters/setters for backward compatibility
    @Deprecated
    public String getDestinationCountry() {
        return country;
    }

    @Deprecated
    public void setDestinationCountry(String destinationCountry) {
        this.country = destinationCountry;
    }
}
