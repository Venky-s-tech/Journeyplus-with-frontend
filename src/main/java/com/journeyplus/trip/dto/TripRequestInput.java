package com.journeyplus.trip.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TripRequestInput {

    @NotBlank(message = "Purpose is required")
    private String purpose;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Departure date is required")
    @JsonAlias("startDate")
    private LocalDate departureDate;

    @NotNull(message = "Return date is required")
    @JsonAlias("endDate")
    private LocalDate returnDate;

    @NotBlank(message = "Travel type is required")
    private String travelType; // DOMESTIC / INTERNATIONAL

    @DecimalMin(value = "0.01", message = "Estimated cost must be positive")
    private BigDecimal estimatedCost;

    private String comments;

    @JsonAlias("approvingManagerUsername")
    private String approverUsername;

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getTravelType() {
        return travelType;
    }

    public void setTravelType(String travelType) {
        this.travelType = travelType;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getApproverUsername() {
        return approverUsername;
    }

    public void setApproverUsername(String approverUsername) {
        this.approverUsername = approverUsername;
    }

    // Deprecated getters/setters for backward compatibility
    @Deprecated
    public LocalDate getStartDate() {
        return departureDate;
    }

    @Deprecated
    public void setStartDate(LocalDate startDate) {
        this.departureDate = startDate;
    }

    @Deprecated
    public LocalDate getEndDate() {
        return returnDate;
    }

    @Deprecated
    public void setEndDate(LocalDate endDate) {
        this.returnDate = endDate;
    }

    @Deprecated
    public String getApprovingManagerUsername() {
        return approverUsername;
    }

    @Deprecated
    public void setApprovingManagerUsername(String approvingManagerUsername) {
        this.approverUsername = approvingManagerUsername;
    }
}
