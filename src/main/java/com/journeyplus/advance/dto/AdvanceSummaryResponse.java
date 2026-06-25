package com.journeyplus.advance.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdvanceSummaryResponse {
    private AdvanceResponse advanceDetails;
    private List<AdvanceSettlementResponse> settlementDetails;
    private BigDecimal totalUtilisedAmount;
    private BigDecimal totalReturnedAmount;
    private BigDecimal outstandingAmount;
    private String currentStatus;

    public AdvanceSummaryResponse() {
    }

    public AdvanceSummaryResponse(AdvanceResponse advanceDetails, List<AdvanceSettlementResponse> settlementDetails, BigDecimal totalUtilisedAmount, BigDecimal totalReturnedAmount, BigDecimal outstandingAmount, String currentStatus) {
        this.advanceDetails = advanceDetails;
        this.settlementDetails = settlementDetails;
        this.totalUtilisedAmount = totalUtilisedAmount;
        this.totalReturnedAmount = totalReturnedAmount;
        this.outstandingAmount = outstandingAmount;
        this.currentStatus = currentStatus;
    }

    public AdvanceResponse getAdvanceDetails() {
        return advanceDetails;
    }

    public void setAdvanceDetails(AdvanceResponse advanceDetails) {
        this.advanceDetails = advanceDetails;
    }

    public List<AdvanceSettlementResponse> getSettlementDetails() {
        return settlementDetails;
    }

    public void setSettlementDetails(List<AdvanceSettlementResponse> settlementDetails) {
        this.settlementDetails = settlementDetails;
    }

    public BigDecimal getTotalUtilisedAmount() {
        return totalUtilisedAmount;
    }

    public void setTotalUtilisedAmount(BigDecimal totalUtilisedAmount) {
        this.totalUtilisedAmount = totalUtilisedAmount;
    }

    public BigDecimal getTotalReturnedAmount() {
        return totalReturnedAmount;
    }

    public void setTotalReturnedAmount(BigDecimal totalReturnedAmount) {
        this.totalReturnedAmount = totalReturnedAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(BigDecimal outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}
