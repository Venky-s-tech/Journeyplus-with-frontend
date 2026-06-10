package com.journeyplus.compliance.entity;

import com.journeyplus.common.EncryptedBigDecimalConverter;
import com.journeyplus.expense.entity.ExpenseLine;
import com.journeyplus.policy.entity.TravelPolicy;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "policy_exceptions")
public class PolicyException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compliance_audit_id", nullable = false)
    private ComplianceAudit complianceAudit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private TravelPolicy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_line_id", nullable = false)
    private ExpenseLine expenseLine;

    @Column(name = "violation_type", nullable = false, length = 100)
    private String violationType; // DAILY_ALLOWANCE_EXCEEDED, TRIP_LIMIT_EXCEEDED

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "amount_exceeded", nullable = false, length = 255)
    private BigDecimal amountExceeded;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(name = "approval_status", nullable = false, length = 50)
    private String approvalStatus = "PENDING"; // PENDING, APPROVED, REJECTED

    public PolicyException() {}

    public PolicyException(ComplianceAudit complianceAudit, TravelPolicy policy, ExpenseLine expenseLine, String violationType, BigDecimal amountExceeded) {
        this.complianceAudit = complianceAudit;
        this.policy = policy;
        this.expenseLine = expenseLine;
        this.violationType = violationType;
        this.amountExceeded = amountExceeded;
        this.approvalStatus = "PENDING";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ComplianceAudit getComplianceAudit() {
        return complianceAudit;
    }

    public void setComplianceAudit(ComplianceAudit complianceAudit) {
        this.complianceAudit = complianceAudit;
    }

    public TravelPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(TravelPolicy policy) {
        this.policy = policy;
    }

    public ExpenseLine getExpenseLine() {
        return expenseLine;
    }

    public void setExpenseLine(ExpenseLine expenseLine) {
        this.expenseLine = expenseLine;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public BigDecimal getAmountExceeded() {
        return amountExceeded;
    }

    public void setAmountExceeded(BigDecimal amountExceeded) {
        this.amountExceeded = amountExceeded;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
}
