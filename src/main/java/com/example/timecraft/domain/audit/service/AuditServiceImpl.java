package com.example.timecraft.domain.audit.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.timecraft.domain.audit.persistence.AuditLogEntity;
import com.example.timecraft.domain.audit.persistence.AuditLogRepository;
import com.example.timecraft.domain.multitenant.config.TenantIdentifierResolver;
import com.example.timecraft.domain.user.persistence.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {
    private final AuditLogRepository auditLogRepository;
    @Value("${multitenancy.tenant.default-tenant}")
    private String defaultTenant;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void logAction(UserEntity user, String action, String entityType, String entityId, String details, Long tenantId) {
        String ipAddress = getClientIpAddress();

        AuditLogEntity auditLog = AuditLogEntity.builder()
            .userId(user != null ? user.getId() : null)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .details(details)
            .tenantId(tenantId)
            .ipAddress(ipAddress)
            .timestamp(LocalDateTime.now())
            .build();

        try {
            // Зберігаємо поточну схему
            String currentSchema = TenantIdentifierResolver.getCurrentTenant();

            try {
                // Тимчасово перемикаємось на публічну схему
                TenantIdentifierResolver.setCurrentTenant(defaultTenant);

                // Зберігаємо лог у публічній схемі
                auditLogRepository.save(auditLog);
            } finally {
                // Повертаємось до попередньої схеми
                TenantIdentifierResolver.setCurrentTenant(currentSchema);
            }
        } catch (Exception e) {
            // Якщо не вдається зберегти через Hibernate/JPA, спробуємо напряму через JDBC

            try {
                // Пряме збереження в таблицю через JDBC
                jdbcTemplate.update(
                    "INSERT INTO public.audit_log (user_id, action, entity_type, entity_id, details, tenant_id, ip_address, timestamp) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    user != null ? user.getId() : null,
                    action,
                    entityType,
                    entityId,
                    details,
                    tenantId,
                    ipAddress,
                    LocalDateTime.now()
                );
            } catch (Exception jdbcError) {
                log.error(jdbcError.getMessage(), jdbcError);
            }
        }
    }


    @Override
    public void logAction(String action, String entityType, String entityId, String details) {
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

        logAction(user, action, entityType, entityId, details, tenantId);
    }

    private String getClientIpAddress() {
        HttpServletRequest request = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }

        if (request == null) {
            return "unknown";
        }

        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }
}