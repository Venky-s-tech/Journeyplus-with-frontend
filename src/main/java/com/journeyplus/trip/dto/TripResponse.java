package com.journeyplus.trip.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TripResponse {
    private Long id;
    private SimpleUserDTO employee;
    private String purpose;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String comments;
    private SimpleUserDTO approvingManager;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ItineraryLegResponse> legs;
    private List<VisaRequirementResponse> visas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SimpleUserDTO getEmployee() { return employee; }
    public void setEmployee(SimpleUserDTO employee) { this.employee = employee; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public SimpleUserDTO getApprovingManager() { return approvingManager; }
    public void setApprovingManager(SimpleUserDTO approvingManager) { this.approvingManager = approvingManager; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<ItineraryLegResponse> getLegs() { return legs; }
    public void setLegs(List<ItineraryLegResponse> legs) { this.legs = legs; }
    public List<VisaRequirementResponse> getVisas() { return visas; }
    public void setVisas(List<VisaRequirementResponse> visas) { this.visas = visas; }
}
