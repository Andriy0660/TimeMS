package com.example.timecraft.domain.audit.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.timecraft.domain.audit.service.AuditService;
import com.example.timecraft.domain.multitenant.config.TenantIdentifierResolver;
import com.example.timecraft.domain.user.persistence.UserEntity;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    private final AuditService auditService;

    @Value("${multitenancy.tenant.default-tenant}")
  private String defaultTenant;

    @Pointcut("execution(* com.example.timecraft.domain.timelog.service.TimeLogService.create(..))")
    public void timeLogCreatePointcut() {}

    @Pointcut("execution(* com.example.timecraft.domain.timelog.service.TimeLogService.delete(..))")
    public void timeLogDeletePointcut() {}

    @Pointcut("execution(* com.example.timecraft.domain.worklog.service.WorklogService.createFromTimeLog(..))")
    public void worklogCreatePointcut() {}

    @Pointcut("execution(* com.example.timecraft.domain.worklog.service.WorklogService.delete(..))")
    public void worklogDeletePointcut() {}

    @AfterReturning("timeLogCreatePointcut()")
    public void afterTimeLogCreate(JoinPoint joinPoint) {
        TenantIdentifierResolver.setCurrentTenant(defaultTenant);
        logAction("TIMELOG_CREATE", "TIMELOG", null, "Time log created");
    }

    @Before("timeLogDeletePointcut() && args(timeLogId)")
    public void beforeTimeLogDelete(JoinPoint joinPoint, long timeLogId) {
        logAction("TIMELOG_DELETE", "TIMELOG", String.valueOf(timeLogId), "Time log deleted");
    }

    @AfterReturning("worklogCreatePointcut()")
    public void afterWorklogCreate(JoinPoint joinPoint) {
        logAction("WORKLOG_CREATE", "WORKLOG", null, "Worklog created");
    }

    @Before("worklogDeletePointcut() && args(issueKey, id)")
    public void beforeWorklogDelete(JoinPoint joinPoint, String issueKey, Long id) {
        logAction("WORKLOG_DELETE", "WORKLOG", id.toString(),
            "Worklog deleted for issue: " + issueKey);
    }

    private void logAction(String action, String entityType, String entityId, String details) {
        TenantIdentifierResolver.setCurrentTenant(defaultTenant);
        UserEntity user = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
            SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserEntity) {
            user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        Long tenantId = null;
        if (user != null && !user.getTenants().isEmpty()) {
            tenantId = user.getTenants().getFirst().getId();
        }

        auditService.logAction(user, action, entityType, entityId, details, tenantId);
    }
}