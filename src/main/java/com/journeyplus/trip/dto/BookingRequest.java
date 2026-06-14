package com.journeyplus.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload to book an itinerary leg")
public class BookingRequest {

    @Schema(description = "Optional booking reference provided by supplier", example = "BR-12345")
    @Size(max = 200)
    private String bookingReference;

    @Schema(description = "Booking status (e.g. BOOKED, PENDING, CANCELLED)", example = "BOOKED")
    @NotBlank
    @Size(max = 50)
    private String bookingStatus;

    public BookingRequest() {
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}
