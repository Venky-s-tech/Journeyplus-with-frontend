package com.journeyplus.trip.dto;

import java.math.BigDecimal;
import java.util.List;

public class TripSummaryResponse {
    private TripResponse tripDetails;
    private List<ItineraryLegResponse> itineraryLegs;
    private List<VisaRequirementResponse> visaDetails;
    private BigDecimal totalEstimatedCost;

    public TripSummaryResponse() {
    }

    public TripSummaryResponse(TripResponse tripDetails, List<ItineraryLegResponse> itineraryLegs, List<VisaRequirementResponse> visaDetails, BigDecimal totalEstimatedCost) {
        this.tripDetails = tripDetails;
        this.itineraryLegs = itineraryLegs;
        this.visaDetails = visaDetails;
        this.totalEstimatedCost = totalEstimatedCost;
    }

    public TripResponse getTripDetails() {
        return tripDetails;
    }

    public void setTripDetails(TripResponse tripDetails) {
        this.tripDetails = tripDetails;
    }

    public List<ItineraryLegResponse> getItineraryLegs() {
        return itineraryLegs;
    }

    public void setItineraryLegs(List<ItineraryLegResponse> itineraryLegs) {
        this.itineraryLegs = itineraryLegs;
    }

    public List<VisaRequirementResponse> getVisaDetails() {
        return visaDetails;
    }

    public void setVisaDetails(List<VisaRequirementResponse> visaDetails) {
        this.visaDetails = visaDetails;
    }

    public BigDecimal getTotalEstimatedCost() {
        return totalEstimatedCost;
    }

    public void setTotalEstimatedCost(BigDecimal totalEstimatedCost) {
        this.totalEstimatedCost = totalEstimatedCost;
    }
}
