package com.journeyplus.trip.dto;

public class VisaRequirementResponse {
    private Long id;
    private String destinationCountry;
    private boolean requiresVisa;
    private String status;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }
    public boolean isRequiresVisa() { return requiresVisa; }
    public void setRequiresVisa(boolean requiresVisa) { this.requiresVisa = requiresVisa; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
