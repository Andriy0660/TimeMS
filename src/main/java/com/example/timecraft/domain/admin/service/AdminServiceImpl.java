package com.example.timecraft.domain.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.admin.dto.AuditLogResponse;
import com.example.timecraft.domain.admin.dto.TenantAuditResponse;
import com.example.timecraft.domain.audit.persistence.AuditLogEntity;
import com.example.timecraft.domain.audit.persistence.AuditLogRepository;
import com.example.timecraft.domain.audit.service.AuditService;
import com.example.timecraft.domain.multitenant.persistence.TenantEntity;
import com.example.timecraft.domain.multitenant.persistence.TenantRepository;
import com.example.timecraft.domain.user.persistence.UserEntity;
import com.example.timecraft.domain.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AuditLogRepository auditLogRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    public TenantAuditResponse getTenantAuditLogs(Long tenantId, Pageable pageable) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new NotFoundException("Tenant not found"));

        Page<AuditLogEntity> auditLogs = auditLogRepository.findByTenantId(tenantId, pageable);
        Page<AuditLogResponse> auditLogResponses = auditLogs.map(this::mapToAuditLogResponse);

        return TenantAuditResponse.builder()
            .tenantId(tenant.getId())
            .schemaName(tenant.getSchemaName())
            .auditLogs(auditLogResponses)
            .build();
    }

    @Override
    public Page<AuditLogResponse> getUserAuditLogs(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        Page<AuditLogEntity> auditLogs = auditLogRepository.findByUserId(userId, pageable);
        return auditLogs.map(this::mapToAuditLogResponse);
    }

    @Override
    public void toggleUserActive(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        boolean newActiveState = !user.isActive();
        user.setActive(newActiveState);
        userRepository.save(user);

        // Логуємо дію в систему аудиту
        auditService.logAction(
            "TOGGLE_USER_ACTIVE",
            "USER",
            userId.toString(),
            "Changed user active state to: " + newActiveState
        );
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLogEntity auditLog) {
        String userEmail = null;
        if (auditLog.getUserId() != null) {
            userEmail = userRepository.findById(auditLog.getUserId())
                .map(UserEntity::getEmail)
                .orElse(null);
        }

        return AuditLogResponse.builder()
            .id(auditLog.getId())
            .userId(auditLog.getUserId())
            .userEmail(userEmail)
            .action(auditLog.getAction())
            .entityType(auditLog.getEntityType())
            .entityId(auditLog.getEntityId())
            .details(auditLog.getDetails())
            .ipAddress(auditLog.getIpAddress())
            .timestamp(auditLog.getTimestamp())
            .build();
    }
}