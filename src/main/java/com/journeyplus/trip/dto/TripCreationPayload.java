package com.journeyplus.trip.dto;

import com.journeyplus.trip.entity.ItineraryLeg;
import com.journeyplus.trip.entity.TripRequest;
import com.journeyplus.trip.entity.VisaRequirement;

import java.util.List;

public class TripCreationPayload {

    private TripRequest tripRequest;
    private List<ItineraryLeg> legs;
    private List<VisaRequirement> visas;

    // Getters and Setters
    public TripRequest getTripRequest() {
        return tripRequest;
    }

    public void setTripRequest(TripRequest tripRequest) {
        this.tripRequest = tripRequest;
    }

    public List<ItineraryLeg> getLegs() {
        return legs;
    }

    public void setLegs(List<ItineraryLeg> legs) {
        this.legs = legs;
    }

    public List<VisaRequirement> getVisas() {
        return visas;
    }

    public void setVisas(List<VisaRequirement> visas) {
        this.visas = visas;
    }
}
