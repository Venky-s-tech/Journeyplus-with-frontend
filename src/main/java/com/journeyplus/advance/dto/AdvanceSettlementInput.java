package com.journeyplus.advance.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AdvanceSettlementInput {

    @NotNull(message = "Amount utilised is required")
    @DecimalMin(value = "0.00", message = "Amount utilised must be non-negative")
    @JsonAlias("actualSpent")
    private BigDecimal amountUtilised;

    @NotNull(message = "Amount returned is required")
    @DecimalMin(value = "0.00", message = "Amount returned must be non-negative")
    @JsonAlias("returnedAmount")
    private BigDecimal amountReturned;

    private String remarks;

    public BigDecimal getAmountUtilised() {
        return amountUtilised;
    }

    public void setAmountUtilised(BigDecimal amountUtilised) {
        this.amountUtilised = amountUtilised;
    }

    public BigDecimal getAmountReturned() {
        return amountReturned;
    }

    public void setAmountReturned(BigDecimal amountReturned) {
        this.amountReturned = amountReturned;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

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
