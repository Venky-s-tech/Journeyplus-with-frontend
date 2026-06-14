package com.journeyplus.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to update or set visa requirement for a trip")
public class VisaRequest {

    @Schema(description = "Destination country (name or ISO code)", example = "India")
    @NotBlank
    private String destinationCountry;

    @Schema(description = "Whether the traveller requires a visa for the destination", example = "true")
    @NotNull
    private Boolean requiresVisa;

    @Schema(description = "Visa application/status (PENDING, APPLIED, APPROVED, EXEMPTED)", example = "PENDING", implementation = VisaStatus.class)
    @NotNull
    private VisaStatus status;

    @Schema(description = "Optional free-text notes about the visa", example = "Appointment scheduled at embassy")
    @Size(max = 2000)
    private String notes;

    public VisaRequest() {
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
