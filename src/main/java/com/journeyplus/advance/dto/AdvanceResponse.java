package com.journeyplus.advance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdvanceResponse {
    private Long id;
    private Long tripRequestId;
    private Long employeeId;
    private BigDecimal requestedAmount;
    private String currency;
    private String purposeDetails;
    private BigDecimal usdEquivalent;
    private String status;
    private Long approvedById;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDate disbursementDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTripRequestId() { return tripRequestId; }
    public void setTripRequestId(Long tripRequestId) { this.tripRequestId = tripRequestId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getPurposeDetails() { return purposeDetails; }
    public void setPurposeDetails(String purposeDetails) { this.purposeDetails = purposeDetails; }
    public BigDecimal getUsdEquivalent() { return usdEquivalent; }
    public void setUsdEquivalent(BigDecimal usdEquivalent) { this.usdEquivalent = usdEquivalent; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    public LocalDate getDisbursementDate() { return disbursementDate; }
    public void setDisbursementDate(LocalDate disbursementDate) { this.disbursementDate = disbursementDate; }

    // Deprecated getters and setters for backward compatibility
    @Deprecated
    public BigDecimal getAmount() { return requestedAmount; }
    @Deprecated
    public void setAmount(BigDecimal amount) { this.requestedAmount = amount; }
    @Deprecated
    public String getOriginalCurrency() { return currency; }
    @Deprecated
    public void setOriginalCurrency(String originalCurrency) { this.currency = originalCurrency; }
    @Deprecated
    public Long getApproverId() { return approvedById; }
    @Deprecated
    public void setApproverId(Long approverId) { this.approvedById = approverId; }
    @Deprecated
    public LocalDate getRequestDate() { return createdDate != null ? createdDate.toLocalDate() : null; }
    @Deprecated
    public void setRequestDate(LocalDate requestDate) { if (requestDate != null) this.createdDate = requestDate.atStartOfDay(); }
}
