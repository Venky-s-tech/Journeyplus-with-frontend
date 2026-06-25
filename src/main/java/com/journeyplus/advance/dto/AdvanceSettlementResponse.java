package com.journeyplus.advance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdvanceSettlementResponse {
    private Long id;
    private Long advanceRequestId;
    private BigDecimal amountUtilised;
    private BigDecimal amountReturned;
    private LocalDate settlementDate;
    private String status;
    private String remarks;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdvanceRequestId() { return advanceRequestId; }
    public void setAdvanceRequestId(Long advanceRequestId) { this.advanceRequestId = advanceRequestId; }
    public BigDecimal getAmountUtilised() { return amountUtilised; }
    public void setAmountUtilised(BigDecimal amountUtilised) { this.amountUtilised = amountUtilised; }
    public BigDecimal getAmountReturned() { return amountReturned; }
    public void setAmountReturned(BigDecimal amountReturned) { this.amountReturned = amountReturned; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    // Deprecated getters and setters for backward compatibility
    @Deprecated
    public BigDecimal getActualSpent() { return amountUtilised; }
    @Deprecated
    public void setActualSpent(BigDecimal actualSpent) { this.amountUtilised = actualSpent; }
    @Deprecated
    public BigDecimal getReturnedAmount() { return amountReturned; }
    @Deprecated
    public void setReturnedAmount(BigDecimal returnedAmount) { this.amountReturned = returnedAmount; }
}
