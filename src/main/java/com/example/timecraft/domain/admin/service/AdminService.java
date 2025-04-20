package com.example.timecraft.domain.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.timecraft.domain.admin.dto.AuditLogResponse;
import com.example.timecraft.domain.admin.dto.TenantAuditResponse;

public interface AdminService {
    TenantAuditResponse getTenantAuditLogs(Long tenantId, Pageable pageable);
    Page<AuditLogResponse> getUserAuditLogs(Long userId, Pageable pageable);
    void toggleUserActive(Long userId);
}