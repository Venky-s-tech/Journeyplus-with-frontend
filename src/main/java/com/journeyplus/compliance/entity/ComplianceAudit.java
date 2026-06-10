package com.journeyplus.compliance.entity;

import com.journeyplus.expense.entity.ExpenseLine;
import com.journeyplus.iam.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_audits")
public class ComplianceAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_line_id", nullable = false)
    private ExpenseLine expenseLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor_id")
    private User auditor;

    @Column(name = "audit_date")
    private LocalDateTime auditDate = LocalDateTime.now();

    @Column(name = "compliance_status", nullable = false, length = 50)
    private String complianceStatus = "PASSED"; // PASSED, FLAG_BREACH

    @Column(name = "violations_found", columnDefinition = "TEXT")
    private String violationsFound;

    @Column(name = "audit_notes", columnDefinition = "TEXT")
    private String auditNotes;

    public ComplianceAudit() {}

    public ComplianceAudit(ExpenseLine expenseLine, User auditor, String complianceStatus, String violationsFound, String auditNotes) {
        this.expenseLine = expenseLine;
        this.auditor = auditor;
        this.complianceStatus = complianceStatus;
        this.violationsFound = violationsFound;
        this.auditNotes = auditNotes;
        this.auditDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExpenseLine getExpenseLine() {
        return expenseLine;
    }

    public void setExpenseLine(ExpenseLine expenseLine) {
        this.expenseLine = expenseLine;
    }

    public User getAuditor() {
        return auditor;
    }

    public void setAuditor(User auditor) {
        this.auditor = auditor;
    }

    public LocalDateTime getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(LocalDateTime auditDate) {
        this.auditDate = auditDate;
    }

    public String getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(String complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    public String getViolationsFound() {
        return violationsFound;
    }

    public void setViolationsFound(String violationsFound) {
        this.violationsFound = violationsFound;
    }

    public String getAuditNotes() {
        return auditNotes;
    }

    public void setAuditNotes(String auditNotes) {
        this.auditNotes = auditNotes;
    }
}
