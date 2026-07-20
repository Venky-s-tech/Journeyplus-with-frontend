package com.journeyplus.compliance.controller;

import com.journeyplus.compliance.entity.PolicyException;
import com.journeyplus.compliance.repository.PolicyExceptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exceptions")
public class ExceptionController {

    private final ComplianceController complianceController;
    private final PolicyExceptionRepository policyExceptionRepository;

    public ExceptionController(ComplianceController complianceController, PolicyExceptionRepository policyExceptionRepository) {
        this.complianceController = complianceController;
        this.policyExceptionRepository = policyExceptionRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPLIANCE','FINANCE','ADMIN')")
    public ResponseEntity<List<PolicyException>> listExceptions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        return complianceController.listExceptions(status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPLIANCE','FINANCE','ADMIN')")
    public ResponseEntity<PolicyException> getException(@PathVariable Long id) {
        PolicyException pe = policyExceptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exception not found: " + id));
        return ResponseEntity.ok(pe);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COMPLIANCE','APPROVING_MANAGER','ADMIN')")
    public ResponseEntity<PolicyException> updateExceptionStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return complianceController.patchExceptionStatus(id, body);
    }
}
