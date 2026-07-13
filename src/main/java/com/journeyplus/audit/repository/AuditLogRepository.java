package com.journeyplus.audit.repository;

import com.journeyplus.audit.entity.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUsername(String username);
    List<AuditLog> findByModule(String module);

    List<AuditLog> findByUsernameAndUser_IdAndActionAndModuleAndTimestampGreaterThanEqualAndTimestampLessThanEqual(
            String username,
            Long userId,
            String action,
            String module,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    default List<AuditLog> searchAuditLogs(
            String username,
            Long userId,
            String action,
            String module,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return findByUsernameAndUser_IdAndActionAndModuleAndTimestampGreaterThanEqualAndTimestampLessThanEqual(
                username, userId, action, module, startDate, endDate, pageable
        );
    }
}





