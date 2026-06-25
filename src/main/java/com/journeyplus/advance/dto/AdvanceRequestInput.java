package com.journeyplus.advance.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AdvanceRequestInput {

    @NotNull(message = "Trip request ID is required")
    private Long tripRequestId;

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "0.01", message = "Requested amount must be positive")
    @JsonAlias("amount")
    private BigDecimal requestedAmount;

    @NotBlank(message = "Currency is required")
    @JsonAlias("originalCurrency")
    private String currency;

    @NotBlank(message = "Purpose details are required")
    private String purposeDetails;

    private BigDecimal usdEquivalent;

    public Long getTripRequestId() {
        return tripRequestId;
    }

    public void setTripRequestId(Long tripRequestId) {
        this.tripRequestId = tripRequestId;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPurposeDetails() {
        return purposeDetails;
    }

    public void setPurposeDetails(String purposeDetails) {
        this.purposeDetails = purposeDetails;
    }

    public BigDecimal getUsdEquivalent() {
        return usdEquivalent;
    }

    public void setUsdEquivalent(BigDecimal usdEquivalent) {
        this.usdEquivalent = usdEquivalent;
    }

    // Deprecated getters and setters for backward compatibility
    @Deprecated
    public BigDecimal getAmount() { return requestedAmount; }
    @Deprecated
    public void setAmount(BigDecimal amount) { this.requestedAmount = amount; }
    @Deprecated
    public String getOriginalCurrency() { return currency; }
    @Deprecated
    public void setOriginalCurrency(String originalCurrency) { this.currency = originalCurrency; }
}
