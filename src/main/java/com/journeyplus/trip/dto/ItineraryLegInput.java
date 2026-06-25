package com.journeyplus.trip.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.journeyplus.trip.entity.LegType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItineraryLegInput {

    @NotBlank(message = "Origin is required")
    @JsonAlias("departureCity")
    private String origin;

    @NotBlank(message = "Destination is required")
    @JsonAlias("arrivalCity")
    private String destination;

    @NotNull(message = "Leg type is required")
    @JsonAlias("travelMode")
    private LegType legType;

    @NotNull(message = "Travel date is required")
    private LocalDate travelDate;

    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
    private String carrierDetails;

    @JsonAlias("bookingReference")
    private String bookingRef;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.01", message = "Cost must be positive")
    @JsonAlias("estimatedCost")
    private BigDecimal cost;

    private String originalCurrency;
    private BigDecimal usdEquivalent;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LegType getLegType() {
        return legType;
    }

    public void setLegType(LegType legType) {
        this.legType = legType;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public LocalDateTime getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(LocalDateTime departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    public LocalDateTime getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(LocalDateTime arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public String getCarrierDetails() {
        return carrierDetails;
    }

    public void setCarrierDetails(String carrierDetails) {
        this.carrierDetails = carrierDetails;
    }

    public String getBookingRef() {
        return bookingRef;
    }

    public void setBookingRef(String bookingRef) {
        this.bookingRef = bookingRef;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public void setOriginalCurrency(String originalCurrency) {
        this.originalCurrency = originalCurrency;
    }

    public BigDecimal getUsdEquivalent() {
        return usdEquivalent;
    }

    public void setUsdEquivalent(BigDecimal usdEquivalent) {
        this.usdEquivalent = usdEquivalent;
    }

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
    public String getTravelMode() { return legType != null ? legType.name() : null; }
    @Deprecated
    public void setTravelMode(String travelMode) { this.legType = LegType.valueOf(travelMode.toUpperCase()); }
    @Deprecated
    public BigDecimal getEstimatedCost() { return cost; }
    @Deprecated
    public void setEstimatedCost(BigDecimal estimatedCost) { this.cost = estimatedCost; }
    @Deprecated
    public String getBookingReference() { return bookingRef; }
    @Deprecated
    public void setBookingReference(String bookingReference) { this.bookingRef = bookingReference; }
}
