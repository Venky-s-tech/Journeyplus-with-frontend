package com.journeyplus.expense.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.journeyplus.compliance.service.PolicyComplianceEngine;
import com.journeyplus.config.AuditAction;
import com.journeyplus.event.StatusChangeEvent;
import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.entity.ExpenseLine;
import com.journeyplus.expense.entity.ExpenseStatus;
import com.journeyplus.expense.entity.Reimbursement;
import com.journeyplus.expense.repository.ExpenseClaimRepository;
import com.journeyplus.expense.repository.ExpenseLineRepository;
import com.journeyplus.expense.repository.ReimbursementRepository;
import com.journeyplus.iam.entity.User;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    private ExpenseLineRepository expenseLineRepository;

    @Autowired
    private ReimbursementRepository reimbursementRepository;

    @Autowired
    private PolicyComplianceEngine complianceEngine;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Standard static conversion rates for multi-currency conversion to USD
    private BigDecimal getExchangeRateToUsd(String currency) {
        if (currency == null) return BigDecimal.ONE;
        return switch (currency.toUpperCase()) {
            case "INR" -> new BigDecimal("0.012");
            case "EUR" -> new BigDecimal("1.08");
            case "GBP" -> new BigDecimal("1.25");
            case "JPY" -> new BigDecimal("0.0064");
            case "CAD" -> new BigDecimal("0.73");
            default -> BigDecimal.ONE;
        };
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "CREATE_EXPENSE_CLAIM")
    public ExpenseClaim createExpenseClaim(ExpenseClaim claim) {
        claim.setStatus(ExpenseStatus.DRAFT);
        claim.setTotalAmount(BigDecimal.ZERO);
        claim.setUsdEquivalent(BigDecimal.ZERO);
        return expenseClaimRepository.save(claim);
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "ADD_EXPENSE_LINE")
    public ExpenseLine addExpenseLine(Long claimId, ExpenseLine line) {
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Can only add expense lines to DRAFT claims");
        }

        // Defensive: do not trust client-supplied identifiers or compliance fields
        line.setId(null);
        // Always associate the persisted claim using path variable
        line.setExpenseClaim(claim);
        // Ensure a non-null policy compliance status before persisting to avoid DB NOT NULL constraint
        if (line.getPolicyComplianceStatus() == null) {
            line.setPolicyComplianceStatus("COMPLIANT");
        }
        // Clear any client-supplied compliance remarks; compliance engine will set them as needed
        line.setComplianceRemarks(null);

        // Validate amount and currency
        if (line.getAmount() == null) throw new IllegalArgumentException("Expense line amount is required");
        if (line.getOriginalCurrency() == null) line.setOriginalCurrency(claim.getOriginalCurrency());

        // Multi-currency calculation
        BigDecimal rate = getExchangeRateToUsd(line.getOriginalCurrency());
        BigDecimal usdEquivalent = line.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        line.setUsdEquivalent(usdEquivalent);

        // 1) Persist ExpenseLine first so subsequent ComplianceAudit/PolicyException can reference its DB id
        ExpenseLine savedLine = expenseLineRepository.save(line);

        // 2) Run policy compliance checks (this will create ComplianceAudit and PolicyException records as needed)
        // The compliance engine expects an ExpenseLine with an expense claim and usdEquivalent set
        complianceEngine.runComplianceCheck(savedLine);

        // 3) Persist any changes made by the compliance engine (policyComplianceStatus, complianceRemarks)
        savedLine = expenseLineRepository.save(savedLine);

        // 4) Update total claim sums after the line has been recorded
        BigDecimal originalTotal = claim.getTotalAmount().add(line.getAmount());
        BigDecimal usdTotal = claim.getUsdEquivalent().add(usdEquivalent);
        claim.setTotalAmount(originalTotal);
        claim.setUsdEquivalent(usdTotal);
        expenseClaimRepository.save(claim);

        return savedLine;
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "SUBMIT_EXPENSE_CLAIM")
    public ExpenseClaim submitExpenseClaim(Long claimId) {
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));

        if (claim.getStatus() != ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT claims can be submitted");
        }

        claim.setStatus(ExpenseStatus.SUBMITTED);
        claim.setSubmittedDate(LocalDate.now());
        ExpenseClaim saved = expenseClaimRepository.save(claim);

        // Notify Approving Manager
        if (claim.getTripRequest().getApprovingManager() != null) {
            eventPublisher.publishEvent(new StatusChangeEvent(
                    claim.getTripRequest().getApprovingManager().getId(),
                    "Expense Claim Submitted",
                    "An expense claim titled '" + claim.getClaimTitle() + "' has been submitted by " + 
                    claim.getEmployee().getUsername() + " and is awaiting your review."
            ));
        }

        return saved;
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "APPROVE_REJECT_EXPENSE_CLAIM")
    public ExpenseClaim approveOrRejectExpenseClaim(Long claimId, ExpenseStatus newStatus, String comments, User manager) {
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));

        if (claim.getStatus() != ExpenseStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED claims can be approved or rejected");
        }

        if (newStatus != ExpenseStatus.APPROVED && newStatus != ExpenseStatus.REJECTED) {
            throw new IllegalArgumentException("Target status must be APPROVED or REJECTED");
        }

        claim.setStatus(newStatus);
        claim.setManagerComments(comments);
        ExpenseClaim saved = expenseClaimRepository.save(claim);

        // Notify Employee
        eventPublisher.publishEvent(new StatusChangeEvent(
                claim.getEmployee().getId(),
                "Expense Claim " + newStatus.name(),
                "Your expense claim '" + claim.getClaimTitle() + "' has been " + newStatus.name().toLowerCase() + "."
        ));

        return saved;
    }

    @Transactional
    @AuditAction(module = "EXPENSE", action = "PAY_REIMBURSEMENT")
    public ExpenseClaim payReimbursement(Long claimId, Reimbursement reimbursement) {
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));

        if (claim.getStatus() != ExpenseStatus.APPROVED) {
            throw new IllegalStateException("Reimbursements can only be disbursed for APPROVED claims");
        }

        claim.setStatus(ExpenseStatus.PAID);
        ExpenseClaim savedClaim = expenseClaimRepository.save(claim);

        reimbursement.setExpenseClaim(savedClaim);
        reimbursement.setRecipient(savedClaim.getEmployee());
        reimbursement.setAmount(savedClaim.getTotalAmount());
        reimbursement.setOriginalCurrency(savedClaim.getOriginalCurrency());
        reimbursement.setUsdEquivalent(savedClaim.getUsdEquivalent());
        reimbursement.setPaymentDate(LocalDate.now());
        reimbursementRepository.save(reimbursement);

        // Notify Employee
        eventPublisher.publishEvent(new StatusChangeEvent(
                claim.getEmployee().getId(),
                "Expense Claim Paid",
                "Your expense claim '" + claim.getClaimTitle() + "' has been fully paid via " + 
                reimbursement.getPaymentMethod() + "."
        ));

        return savedClaim;
    }

    public ExpenseClaim getExpenseClaim(Long id) {
        return expenseClaimRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));
    }

    public List<ExpenseClaim> getClaimsByEmployee(Long employeeId) {
        return expenseClaimRepository.findByEmployee_Id(employeeId);
    }

    public List<ExpenseLine> getLinesByClaim(Long claimId) {
        return expenseLineRepository.findByExpenseClaim_Id(claimId);
    }
}
