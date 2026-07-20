package com.journeyplus.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TripResponse {
    private Long id;
    private SimpleUserDTO employee;
    private String purpose;
    private String destination;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private String travelType;
    private BigDecimal estimatedCost;
    private String status;
    private String bookingStatus;
    private String workflowStage;
    private String travelDeskStatus;
    private String comments;
    private SimpleUserDTO approver;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getWorkflowStage() { return workflowStage; }
    public void setWorkflowStage(String workflowStage) { this.workflowStage = workflowStage; }

    public String getTravelDeskStatus() { return travelDeskStatus; }
    public void setTravelDeskStatus(String travelDeskStatus) { this.travelDeskStatus = travelDeskStatus; }

    public SimpleUserDTO getEmployee() { return employee; }
    public void setEmployee(SimpleUserDTO employee) { this.employee = employee; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getTravelType() { return travelType; }
    public void setTravelType(String travelType) { this.travelType = travelType; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public SimpleUserDTO getApprover() { return approver; }
    public void setApprover(SimpleUserDTO approver) { this.approver = approver; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    // Deprecated getters and setters for backward compatibility
    @Deprecated
    public LocalDate getStartDate() { return departureDate; }
    @Deprecated
    public void setStartDate(LocalDate startDate) { this.departureDate = startDate; }
    @Deprecated
    public LocalDate getEndDate() { return returnDate; }
    @Deprecated
    public void setEndDate(LocalDate endDate) { this.returnDate = endDate; }
    @Deprecated
    public SimpleUserDTO getApprovingManager() { return approver; }
    @Deprecated
    public void setApprovingManager(SimpleUserDTO approvingManager) { this.approver = approvingManager; }
    @Deprecated
    public LocalDateTime getCreatedAt() { return createdDate; }
    @Deprecated
    public void setCreatedAt(LocalDateTime createdAt) { this.createdDate = createdAt; }
    @Deprecated
    public LocalDateTime getUpdatedAt() { return updatedDate; }
    @Deprecated
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedDate = updatedAt; }
}