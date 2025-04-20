package com.example.timecraft.domain.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.admin.dto.AuditLogResponse;
import com.example.timecraft.domain.admin.dto.TenantAuditResponse;
import com.example.timecraft.domain.admin.service.AdminService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/audit/tenant/{tenantId}")
    public TenantAuditResponse getTenantAuditLogs(@PathVariable Long tenantId, Pageable pageable) {
        return adminService.getTenantAuditLogs(tenantId, pageable);
    }

    @GetMapping("/audit/user/{userId}")
    public Page<AuditLogResponse> getUserAuditLogs(@PathVariable Long userId, Pageable pageable) {
        return adminService.getUserAuditLogs(userId, pageable);
    }

    @PostMapping("/users/{userId}/toggle-active")
    public void toggleUserActive(@PathVariable Long userId) {
        adminService.toggleUserActive(userId);
    }
}