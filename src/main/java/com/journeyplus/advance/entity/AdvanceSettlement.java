package com.journeyplus.advance.entity;

import com.journeyplus.common.EncryptedBigDecimalConverter;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "advance_settlements")
public class AdvanceSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advance_request_id", nullable = false, unique = true)
    private AdvanceRequest advanceRequest;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "actual_spent", nullable = false, length = 255)
    private BigDecimal actualSpent;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "returned_amount", nullable = false, length = 255)
    private BigDecimal returnedAmount;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate = LocalDate.now();

    @Column(nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, SETTLED, AUDITED

    @Column(columnDefinition = "TEXT")
    private String remarks;

    public AdvanceSettlement() {}

    public AdvanceSettlement(AdvanceRequest advanceRequest, BigDecimal actualSpent, BigDecimal returnedAmount, String status, String remarks) {
        this.advanceRequest = advanceRequest;
        this.actualSpent = actualSpent;
        this.returnedAmount = returnedAmount;
        this.settlementDate = LocalDate.now();
        this.status = status;
        this.remarks = remarks;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AdvanceRequest getAdvanceRequest() {
        return advanceRequest;
    }

    public void setAdvanceRequest(AdvanceRequest advanceRequest) {
        this.advanceRequest = advanceRequest;
    }

    public BigDecimal getActualSpent() {
        return actualSpent;
    }

    public void setActualSpent(BigDecimal actualSpent) {
        this.actualSpent = actualSpent;
    }

    public BigDecimal getReturnedAmount() {
        return returnedAmount;
    }

    public void setReturnedAmount(BigDecimal returnedAmount) {
        this.returnedAmount = returnedAmount;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
