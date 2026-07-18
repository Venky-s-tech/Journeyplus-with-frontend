package com.journeyplus.audit.service;

import com.journeyplus.audit.entity.AuditLog;
import com.journeyplus.audit.repository.AuditLogRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Null-safe, most-recent-first audit search. Any null filter is ignored.
     * Reproduces the previous repository query behaviour: optional filtering on each
     * criterion, ordering by timestamp descending, and page/size pagination.
     */
    public List<AuditLog> searchAuditLogs(
            String username,
            Long userId,
            String action,
            String module,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return auditLogRepository.findAll().stream()
                .filter(a -> username == null || (a.getUsername() != null && a.getUsername().equalsIgnoreCase(username)))
                .filter(a -> userId == null || (a.getUser() != null && userId.equals(a.getUser().getId())))
                .filter(a -> action == null || (a.getAction() != null && a.getAction().equalsIgnoreCase(action)))
                .filter(a -> module == null || (a.getModule() != null && a.getModule().equalsIgnoreCase(module)))
                .filter(a -> startDate == null || (a.getTimestamp() != null && !a.getTimestamp().isBefore(startDate)))
                .filter(a -> endDate == null || (a.getTimestamp() != null && !a.getTimestamp().isAfter(endDate)))
                .sorted(Comparator.comparing(AuditLog::getTimestamp).reversed())
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .toList();
    }
}
