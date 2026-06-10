package com.journeyplus.trip.dto;

public class VisaRequirementInput {

    private String destinationCountry;
    private boolean requiresVisa;
    private String notes;

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
