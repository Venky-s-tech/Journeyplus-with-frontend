package com.journeyplus.trip.dto;

import java.util.List;

public class TripCreationRequest {

    private TripRequestInput tripRequest;
    private List<ItineraryLegInput> legs;
    private List<VisaRequirementInput> visas;

    public TripRequestInput getTripRequest() {
        return tripRequest;
    }

    public void setTripRequest(TripRequestInput tripRequest) {
        this.tripRequest = tripRequest;
    }

    public List<ItineraryLegInput> getLegs() {
        return legs;
    }

    public void setLegs(List<ItineraryLegInput> legs) {
        this.legs = legs;
    }

    public List<VisaRequirementInput> getVisas() {
        return visas;
    }

    public void setVisas(List<VisaRequirementInput> visas) {
        this.visas = visas;
    }
}
