package com.journeyplus.compliance.controller;

import com.journeyplus.compliance.entity.AuditOutcome;
import com.journeyplus.compliance.entity.AuditStatus;
import com.journeyplus.compliance.entity.ComplianceAudit;
import com.journeyplus.compliance.entity.PolicyException;
import com.journeyplus.compliance.repository.ComplianceAuditRepository;
import com.journeyplus.compliance.repository.PolicyExceptionRepository;
import com.journeyplus.config.AuditAction;
import com.journeyplus.expense.entity.ExpenseClaim;
import com.journeyplus.expense.entity.ExpenseLineStatus;
import com.journeyplus.expense.entity.ExpenseStatus;
import com.journeyplus.expense.repository.ExpenseClaimRepository;
import com.journeyplus.expense.repository.ExpenseLineRepository;
import com.journeyplus.expense.service.ExpenseService;
import com.journeyplus.iam.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {

    private final PolicyExceptionRepository policyExceptionRepository;
    private final ComplianceAuditRepository complianceAuditRepository;
    private final ExpenseClaimRepository expenseClaimRepository;
    private final ExpenseLineRepository expenseLineRepository;
    private final ExpenseService expenseService;

    public ComplianceController(
            PolicyExceptionRepository policyExceptionRepository,
            ComplianceAuditRepository complianceAuditRepository,
            ExpenseClaimRepository expenseClaimRepository,
            ExpenseLineRepository expenseLineRepository,
            ExpenseService expenseService) {
        this.policyExceptionRepository = policyExceptionRepository;
        this.complianceAuditRepository = complianceAuditRepository;
        this.expenseClaimRepository = expenseClaimRepository;
        this.expenseLineRepository = expenseLineRepository;
        this.expenseService = expenseService;
    }

    @GetMapping("/exceptions")
    @PreAuthorize("hasAnyRole('COMPLIANCE','FINANCE','ADMIN')")
    public ResponseEntity<List<PolicyException>> listExceptions(@RequestParam(required = false) String status) {
        if (status == null) {
            return ResponseEntity.ok(policyExceptionRepository.findAll());
        }
        return ResponseEntity.ok(policyExceptionRepository.findByApprovalStatus(status));
    }

    @PostMapping("/exceptions/{id}/resolve")
    @PreAuthorize("hasAnyRole('COMPLIANCE','APPROVING_MANAGER')")
    @AuditAction(module = "COMPLIANCE", action = "RESOLVE_EXCEPTION")
    public ResponseEntity<PolicyException> resolveException(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String justification) {
        PolicyException e = policyExceptionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Exception not found"));
        if ("APPROVE".equalsIgnoreCase(action)) {
            e.setApprovalStatus("APPROVED");
            if (e.getExpenseLine() != null) {
                e.getExpenseLine().setStatus(ExpenseLineStatus.INCLUDED);
                e.getExpenseLine().setPolicyCompliant(true);
                expenseLineRepository.save(e.getExpenseLine());
            }
        } else if ("REJECT".equalsIgnoreCase(action)) {
            e.setApprovalStatus("REJECTED");
            if (e.getExpenseLine() != null) {
                e.getExpenseLine().setStatus(ExpenseLineStatus.REJECTED);
                e.getExpenseLine().setPolicyCompliant(false);
                expenseLineRepository.save(e.getExpenseLine());
            }
        } else {
            throw new IllegalArgumentException("Invalid action");
        }
        if (justification != null) e.setJustification(justification);
        policyExceptionRepository.save(e);

        if (e.getClaim() != null) {
            expenseService.recomputeClaimTotals(e.getClaim());
        }

        return ResponseEntity.ok(e);
    }

    @PatchMapping("/exceptions/{id}/status")
    @PreAuthorize("hasAnyRole('COMPLIANCE','APPROVING_MANAGER','ADMIN')")
    public ResponseEntity<PolicyException> patchExceptionStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        String status = body.get("status");
        String remarks = body.get("remarks");
        String action = "APPROVE".equalsIgnoreCase(status) ? "APPROVE" : "REJECT";
        return resolveException(id, action, remarks);
    }

    @GetMapping("/claims/high-value")
    @PreAuthorize("hasAnyRole('COMPLIANCE','FINANCE','ADMIN')")
    public ResponseEntity<List<ExpenseClaim>> getHighValueClaims(
            @RequestParam(defaultValue = "5000") java.math.BigDecimal threshold) {
        List<ExpenseClaim> claims = expenseClaimRepository.findAll().stream()
                .filter(c -> c.getTotalAmount() != null && c.getTotalAmount().compareTo(threshold) >= 0)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(claims);
    }

    @PostMapping("/claims/{claimId}/audit")
    @PreAuthorize("hasRole('COMPLIANCE')")
    @AuditAction(module = "COMPLIANCE", action = "CREATE_CLAIM_AUDIT")
    public ResponseEntity<ComplianceAudit> auditClaim(
            @PathVariable Long claimId,
            @RequestParam String findings,
            @RequestParam AuditOutcome outcome,
            @RequestParam AuditStatus status,
            @AuthenticationPrincipal User auditor) {

        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));

        if (claim.getStatus() == ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Only claims that have been submitted or processed can be audited");
        }

        ComplianceAudit audit = new ComplianceAudit();
        audit.setClaim(claim);
        audit.setAuditor(auditor);
        audit.setFindings(findings);
        audit.setAuditOutcome(outcome);
        audit.setStatus(status);
        audit.setAuditDate(LocalDateTime.now());
        audit.setComplianceStatus(outcome == AuditOutcome.Clean ? "PASSED" : "FLAG_BREACH");

        ComplianceAudit saved = complianceAuditRepository.save(audit);
        return ResponseEntity.ok(saved);
    }
}
