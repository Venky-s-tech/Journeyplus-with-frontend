package com.journeyplus.config;

import com.journeyplus.audit.entity.AuditLog;
import com.journeyplus.audit.repository.AuditLogRepository;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditAspect(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @AfterReturning(pointcut = "@annotation(auditAction)", returning = "result")
    public void logAudit(JoinPoint joinPoint, AuditAction auditAction, Object result) {
        String username = "SYSTEM";
        User user = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
            user = userRepository.findByUsername(username).orElse(null);
        }

        // SAFE: only log method signature — never serialize args (may contain passwords or Spring proxies)
        String details = "Method: " + joinPoint.getSignature().toShortString();

        AuditLog log = new AuditLog(user, username, auditAction.action(), auditAction.module(), details);
        auditLogRepository.save(log);
    }
}
