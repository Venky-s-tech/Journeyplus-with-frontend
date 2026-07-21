package com.journeyplus.expense.entity;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
 
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.journeyplus.common.EncryptedBigDecimalConverter;
 
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
 
@Entity
@Table(name = "expense_lines")
@Getter
@Setter
public class ExpenseLine {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_claim_id", nullable = false)
    @JsonIgnore
    private ExpenseClaim expenseClaim;
 
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;
 
    @Column(nullable = false, length = 50)
    private String category; // ACCOMMODATION, TRANSPORT, MEALS, MISC
 
    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(nullable = false, length = 255)
    private BigDecimal amount;
 
    @Column(name = "original_currency", nullable = false, length = 10)
    private String originalCurrency;
 
    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "usd_equivalent", nullable = false, length = 255)
    private BigDecimal usdEquivalent;
 
    @Column(name = "receipt_path")
    private String receiptPath;
 
    @Column(name = "receipt_ref")
    private String receiptRef;
 
    @Column(name = "merchant", length = 150)
    private String merchant;
 
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
 
    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;
 
    @Column(name = "policy_compliance_status", nullable = false, length = 50)
    private String policyComplianceStatus = "COMPLIANT"; // COMPLIANT, NON_COMPLIANT
 
    @Column(name = "compliance_remarks", columnDefinition = "TEXT")
    private String complianceRemarks;
 
    @Column(name = "policy_compliant", nullable = false)
    private boolean policyCompliant = true;
 
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExpenseLineStatus status = ExpenseLineStatus.INCLUDED;
 
    public ExpenseLine() {}
 
    public ExpenseLine(ExpenseClaim expenseClaim, LocalDate expenseDate, String category, BigDecimal amount, String originalCurrency, BigDecimal usdEquivalent, String receiptPath) {
        this.expenseClaim = expenseClaim;
        this.expenseDate = expenseDate;
        this.category = category;
        this.amount = amount;
        this.originalCurrency = originalCurrency;
        this.usdEquivalent = usdEquivalent;
        this.receiptPath = receiptPath;
        this.receiptRef = receiptPath;
        this.policyComplianceStatus = "COMPLIANT";
        this.policyCompliant = true;
        this.status = ExpenseLineStatus.INCLUDED;
    }
 
    public String getReceiptRef() {
        if (receiptRef != null && !receiptRef.isBlank()) {
            return receiptRef;
        }
        return receiptPath;
    }
 
    public void setReceiptRef(String receiptRef) {
        this.receiptRef = receiptRef;
        if (this.receiptPath == null || this.receiptPath.isBlank()) {
            this.receiptPath = receiptRef;
        }
    }
}
