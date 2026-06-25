package com.journeyplus.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItineraryLegResponse {
    private Long id;
    private String origin;
    private String destination;
    private String legType;
    private LocalDate travelDate;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
    private String carrierDetails;
    private String bookingRef;
    private BigDecimal cost;
    private String originalCurrency;
    private BigDecimal usdEquivalent;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getLegType() { return legType; }
    public void setLegType(String legType) { this.legType = legType; }
    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
    public LocalDateTime getDepartureDateTime() { return departureDateTime; }
    public void setDepartureDateTime(LocalDateTime departureDateTime) { this.departureDateTime = departureDateTime; }
    public LocalDateTime getArrivalDateTime() { return arrivalDateTime; }
    public void setArrivalDateTime(LocalDateTime arrivalDateTime) { this.arrivalDateTime = arrivalDateTime; }
    public String getCarrierDetails() { return carrierDetails; }
    public void setCarrierDetails(String carrierDetails) { this.carrierDetails = carrierDetails; }
    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public String getOriginalCurrency() { return originalCurrency; }
    public void setOriginalCurrency(String originalCurrency) { this.originalCurrency = originalCurrency; }
    public BigDecimal getUsdEquivalent() { return usdEquivalent; }
    public void setUsdEquivalent(BigDecimal usdEquivalent) { this.usdEquivalent = usdEquivalent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Deprecated getters and setters for backward compatibility
    @Deprecated
    public String getDepartureCity() { return origin; }
    @Deprecated
    public void setDepartureCity(String departureCity) { this.origin = departureCity; }
    @Deprecated
    public String getArrivalCity() { return destination; }
    @Deprecated
    public void setArrivalCity(String arrivalCity) { this.destination = arrivalCity; }
    @Deprecated
    public String getTravelMode() { return legType; }
    @Deprecated
    public void setTravelMode(String travelMode) { this.legType = travelMode; }
    @Deprecated
    public BigDecimal getEstimatedCost() { return cost; }
    @Deprecated
    public void setEstimatedCost(BigDecimal estimatedCost) { this.cost = estimatedCost; }
    @Deprecated
    public String getBookingReference() { return bookingRef; }
    @Deprecated
    public void setBookingReference(String bookingReference) { this.bookingRef = bookingReference; }
    @Deprecated
    public String getBookingStatus() { return status; }
    @Deprecated
    public void setBookingStatus(String bookingStatus) { this.status = bookingStatus; }
}
