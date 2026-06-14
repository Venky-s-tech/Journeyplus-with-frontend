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

    @GetMapping("/exceptions")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','FINANCE_EXECUTIVE','TRAVEL_ADMIN')")
    public ResponseEntity<List<PolicyException>> listExceptions(@RequestParam(required = false) String status) {
        if (status == null) {
            return ResponseEntity.ok(policyExceptionRepository.findAll());
        }
        return ResponseEntity.ok(policyExceptionRepository.findByApprovalStatus(status));
    }

    @PostMapping("/exceptions/{id}/resolve")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','APPROVING_MANAGER')")
    public ResponseEntity<PolicyException> resolveException(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String justification) {
        PolicyException e = policyExceptionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Exception not found"));
        if ("APPROVE".equalsIgnoreCase(action)) {
            e.setApprovalStatus("APPROVED");
        } else if ("REJECT".equalsIgnoreCase(action)) {
            e.setApprovalStatus("REJECTED");
        } else {
            throw new IllegalArgumentException("Invalid action");
        }
        if (justification != null) e.setJustification(justification);
        policyExceptionRepository.save(e);
        return ResponseEntity.ok(e);
    }
}
