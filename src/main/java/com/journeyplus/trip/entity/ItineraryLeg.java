package com.journeyplus.trip.entity;

import com.journeyplus.common.EncryptedBigDecimalConverter;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "itinerary_legs")
public class ItineraryLeg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_request_id", nullable = false)
    private TripRequest tripRequest;

    @Column(name = "departure_city", nullable = false, length = 100)
    private String departureCity;

    @Column(name = "arrival_city", nullable = false, length = 100)
    private String arrivalCity;

    @Column(name = "travel_mode", nullable = false, length = 50)
    private String travelMode; // FLIGHT, TRAIN, CAB, BUS

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "carrier_details", length = 150)
    private String carrierDetails;

    @Column(name = "booking_reference", length = 100)
    private String bookingReference;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "estimated_cost", nullable = false, length = 255)
    private BigDecimal estimatedCost;

    @Column(name = "original_currency", nullable = false, length = 10)
    private String originalCurrency;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "usd_equivalent", nullable = false, length = 255)
    private BigDecimal usdEquivalent;

    @Column(name = "booking_status", nullable = false, length = 50)
    private String bookingStatus = "PENDING"; // PENDING, BOOKED, CANCELLED

    public ItineraryLeg() {}

    public ItineraryLeg(TripRequest tripRequest, String departureCity, String arrivalCity, String travelMode, LocalDate travelDate, BigDecimal estimatedCost, String originalCurrency, BigDecimal usdEquivalent) {
        this.tripRequest = tripRequest;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.travelMode = travelMode;
        this.travelDate = travelDate;
        this.estimatedCost = estimatedCost;
        this.originalCurrency = originalCurrency;
        this.usdEquivalent = usdEquivalent;
        this.bookingStatus = "PENDING";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TripRequest getTripRequest() {
        return tripRequest;
    }

    public void setTripRequest(TripRequest tripRequest) {
        this.tripRequest = tripRequest;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public String getCarrierDetails() {
        return carrierDetails;
    }

    public void setCarrierDetails(String carrierDetails) {
        this.carrierDetails = carrierDetails;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
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

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}
