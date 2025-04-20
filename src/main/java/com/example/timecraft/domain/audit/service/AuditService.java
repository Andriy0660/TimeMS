package com.example.timecraft.domain.audit.service;

import com.example.timecraft.domain.user.persistence.UserEntity;

public interface AuditService {
    void logAction(UserEntity user, String action, String entityType, String entityId, String details, Long tenantId);
    void logAction(String action, String entityType, String entityId, String details);
}