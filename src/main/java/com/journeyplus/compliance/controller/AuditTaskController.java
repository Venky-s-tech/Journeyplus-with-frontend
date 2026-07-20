package com.journeyplus.compliance.controller;

import com.journeyplus.compliance.entity.ComplianceAudit;
import com.journeyplus.compliance.repository.ComplianceAuditRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditTaskController {

    private final ComplianceAuditRepository complianceAuditRepository;

    public AuditTaskController(ComplianceAuditRepository complianceAuditRepository) {
        this.complianceAuditRepository = complianceAuditRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN')")
    public ResponseEntity<List<ComplianceAudit>> getAllAudits() {
        return ResponseEntity.ok(complianceAuditRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPLIANCE','ADMIN')")
    public ResponseEntity<ComplianceAudit> getAuditById(@PathVariable Long id) {
        ComplianceAudit audit = complianceAuditRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Audit not found: " + id));
        return ResponseEntity.ok(audit);
    }
}
