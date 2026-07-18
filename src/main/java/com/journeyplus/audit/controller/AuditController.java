package com.journeyplus.audit.controller;

import com.journeyplus.audit.entity.AuditLog;
import com.journeyplus.audit.service.AuditService;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','COMPLIANCE')")
    public ResponseEntity<List<AuditLog>> query(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        List<AuditLog> logs = auditService.searchAuditLogs(
                username, userId, action, module, startDate, endDate, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(logs);
    }
}
