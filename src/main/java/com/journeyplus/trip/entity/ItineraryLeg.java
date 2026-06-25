package com.journeyplus.trip.entity;

import com.journeyplus.common.EncryptedBigDecimalConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "itinerary_legs")
@Getter
@Setter
public class ItineraryLeg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Trip request is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_request_id", nullable = false)
    private TripRequest tripRequest;

    @NotBlank(message = "Origin is required")
    @Column(name = "origin", nullable = false, length = 100)
    private String origin;

    @NotBlank(message = "Destination is required")
    @Column(name = "destination", nullable = false, length = 100)
    private String destination;

    @NotNull(message = "Leg type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "leg_type", nullable = false, length = 50)
    private LegType legType;

    @NotNull(message = "Travel date is required")
    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "departure_date_time")
    private LocalDateTime departureDateTime;

    @Column(name = "arrival_date_time")
    private LocalDateTime arrivalDateTime;

    @Column(name = "carrier_details", length = 150)
    private String carrierDetails;

    @Column(name = "booking_ref", length = 100)
    private String bookingRef;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.01", message = "Cost must be positive")
    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "cost", nullable = false, length = 255)
    private BigDecimal cost;

    @Column(name = "original_currency", nullable = false, length = 10)
    private String originalCurrency;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "usd_equivalent", nullable = false, length = 255)
    private BigDecimal usdEquivalent;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ItineraryStatus status = ItineraryStatus.PLANNED;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public ItineraryLeg() {}

    public ItineraryLeg(TripRequest tripRequest, String origin, String destination, LegType legType, LocalDate travelDate, BigDecimal cost, String originalCurrency, BigDecimal usdEquivalent) {
        this.tripRequest = tripRequest;
        this.origin = origin;
        this.destination = destination;
        this.legType = legType;
        this.travelDate = travelDate;
        this.cost = cost;
        this.originalCurrency = originalCurrency;
        this.usdEquivalent = usdEquivalent;
        this.status = ItineraryStatus.PLANNED;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
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
    @Deprecated
    public String getBookingStatus() { return status != null ? status.name() : null; }
    @Deprecated
    public void setBookingStatus(String bookingStatus) {
        if (bookingStatus != null) {
            try {
                if ("CONFIRMED".equalsIgnoreCase(bookingStatus) || "BOOKED".equalsIgnoreCase(bookingStatus)) {
                    this.status = ItineraryStatus.CONFIRMED;
                } else if ("CANCELLED".equalsIgnoreCase(bookingStatus)) {
                    this.status = ItineraryStatus.CANCELLED;
                } else {
                    this.status = ItineraryStatus.valueOf(bookingStatus.toUpperCase());
                }
            } catch (Exception e) {
                this.status = ItineraryStatus.CONFIRMED;
            }
        }
    }
}
