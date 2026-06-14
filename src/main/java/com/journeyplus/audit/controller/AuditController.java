package com.journeyplus.audit.controller;

import com.journeyplus.audit.entity.AuditLog;
import com.journeyplus.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("")
    @PreAuthorize("hasAnyRole('TRAVEL_ADMIN','FINANCE_EXECUTIVE','COMPLIANCE_OFFICER')")
    public ResponseEntity<List<AuditLog>> query(
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        if (username != null && !username.isEmpty()) {
            return ResponseEntity.ok(auditLogRepository.findByUsername(username));
        }

        if (module != null && !module.isEmpty()) {
            return ResponseEntity.ok(auditLogRepository.findByModule(module));
        }

        return ResponseEntity.ok(auditLogRepository.findAll(PageRequest.of(page, size)).getContent());
    }
}
