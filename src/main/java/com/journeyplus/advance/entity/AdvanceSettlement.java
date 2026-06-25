package com.journeyplus.advance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.journeyplus.common.EncryptedBigDecimalConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "advance_settlements")
@Getter
@Setter
public class AdvanceSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Advance request is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advance_request_id", nullable = false)
    @JsonIgnore
    private AdvanceRequest advanceRequest;

    @NotNull(message = "Amount utilised is required")
    @DecimalMin(value = "0.00", message = "Amount utilised must be non-negative")
    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "amount_utilised", nullable = false, length = 255)
    private BigDecimal amountUtilised;

    @NotNull(message = "Amount returned is required")
    @DecimalMin(value = "0.00", message = "Amount returned must be non-negative")
    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "amount_returned", nullable = false, length = 255)
    private BigDecimal amountReturned;

    @NotNull(message = "Settlement date is required")
    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate = LocalDate.now();

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private SettlementStatus status = SettlementStatus.SETTLED;

    @Column(columnDefinition = "TEXT")
    private String remarks;

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

    public AdvanceSettlement() {}

    public AdvanceSettlement(AdvanceRequest advanceRequest, BigDecimal amountUtilised, BigDecimal amountReturned, SettlementStatus status, String remarks) {
        this.advanceRequest = advanceRequest;
        this.amountUtilised = amountUtilised;
        this.amountReturned = amountReturned;
        this.settlementDate = LocalDate.now();
        this.status = status;
        this.remarks = remarks;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Deprecated getters and setters for backward compatibility
    @Deprecated
    @JsonIgnore
    public BigDecimal getActualSpent() { return amountUtilised; }
    @Deprecated
    @JsonIgnore
    public void setActualSpent(BigDecimal actualSpent) { this.amountUtilised = actualSpent; }
    @Deprecated
    @JsonIgnore
    public BigDecimal getReturnedAmount() { return amountReturned; }
    @Deprecated
    @JsonIgnore
    public void setReturnedAmount(BigDecimal returnedAmount) { this.amountReturned = returnedAmount; }
}
