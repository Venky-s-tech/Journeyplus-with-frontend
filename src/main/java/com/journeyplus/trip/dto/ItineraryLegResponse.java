package com.journeyplus.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ItineraryLegResponse {
    private Long id;
    private String departureCity;
    private String arrivalCity;
    private String travelMode;
    private LocalDate travelDate;
    private BigDecimal estimatedCost;
    private String originalCurrency;
    private BigDecimal usdEquivalent;
    private String carrierDetails;
    private String bookingReference;
    private String bookingStatus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDepartureCity() { return departureCity; }
    public void setDepartureCity(String departureCity) { this.departureCity = departureCity; }
    public String getArrivalCity() { return arrivalCity; }
    public void setArrivalCity(String arrivalCity) { this.arrivalCity = arrivalCity; }
    public String getTravelMode() { return travelMode; }
    public void setTravelMode(String travelMode) { this.travelMode = travelMode; }
    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }
    public String getOriginalCurrency() { return originalCurrency; }
    public void setOriginalCurrency(String originalCurrency) { this.originalCurrency = originalCurrency; }
    public BigDecimal getUsdEquivalent() { return usdEquivalent; }
    public void setUsdEquivalent(BigDecimal usdEquivalent) { this.usdEquivalent = usdEquivalent; }
    public String getCarrierDetails() { return carrierDetails; }
    public void setCarrierDetails(String carrierDetails) { this.carrierDetails = carrierDetails; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
}
