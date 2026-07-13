package com.journeyplus.compliance.controller;

import com.journeyplus.compliance.entity.PolicyException;
import com.journeyplus.compliance.repository.PolicyExceptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {

    @Autowired
    private PolicyExceptionRepository policyExceptionRepository;

    @Autowired
    private com.journeyplus.compliance.repository.ComplianceAuditRepository complianceAuditRepository;

    @Autowired
    private com.journeyplus.expense.repository.ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    private com.journeyplus.expense.repository.ExpenseLineRepository expenseLineRepository;

    @Autowired
    private com.journeyplus.expense.service.ExpenseService expenseService;

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
    @com.journeyplus.config.AuditAction(module = "COMPLIANCE", action = "RESOLVE_EXCEPTION")
    public ResponseEntity<PolicyException> resolveException(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String justification) {
        PolicyException e = policyExceptionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Exception not found"));
        if ("APPROVE".equalsIgnoreCase(action)) {
            e.setApprovalStatus("APPROVED");
            if (e.getExpenseLine() != null) {
                e.getExpenseLine().setStatus(com.journeyplus.expense.entity.ExpenseLineStatus.INCLUDED);
                e.getExpenseLine().setPolicyCompliant(true);
                expenseLineRepository.save(e.getExpenseLine());
            }
        } else if ("REJECT".equalsIgnoreCase(action)) {
            e.setApprovalStatus("REJECTED");
            if (e.getExpenseLine() != null) {
                e.getExpenseLine().setStatus(com.journeyplus.expense.entity.ExpenseLineStatus.REJECTED);
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

    @PostMapping("/claims/{claimId}/audit")
    @PreAuthorize("hasRole('COMPLIANCE')")
    @com.journeyplus.config.AuditAction(module = "COMPLIANCE", action = "CREATE_CLAIM_AUDIT")
    public ResponseEntity<com.journeyplus.compliance.entity.ComplianceAudit> auditClaim(
            @PathVariable Long claimId,
            @RequestParam String findings,
            @RequestParam com.journeyplus.compliance.entity.AuditOutcome outcome,
            @RequestParam com.journeyplus.compliance.entity.AuditStatus status,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.journeyplus.iam.entity.User auditor) {
        
        com.journeyplus.expense.entity.ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found"));

        if (claim.getStatus() == com.journeyplus.expense.entity.ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Only claims that have been submitted or processed can be audited");
        }

        com.journeyplus.compliance.entity.ComplianceAudit audit = new com.journeyplus.compliance.entity.ComplianceAudit();
        audit.setClaim(claim);
        audit.setAuditor(auditor);
        audit.setFindings(findings);
        audit.setAuditOutcome(outcome);
        audit.setStatus(status);
        audit.setAuditDate(java.time.LocalDateTime.now());
        audit.setComplianceStatus(outcome == com.journeyplus.compliance.entity.AuditOutcome.Clean ? "PASSED" : "FLAG_BREACH");
        
        com.journeyplus.compliance.entity.ComplianceAudit saved = complianceAuditRepository.save(audit);
        return ResponseEntity.ok(saved);
    }
}
