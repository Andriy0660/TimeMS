package com.example.timecraft.domain.manager;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.manager.dto.TenantInfoResponse;
import com.example.timecraft.domain.manager.dto.UserInfoResponse;
import com.example.timecraft.domain.manager.service.ManagerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manager")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
public class ManagerController {
    private final ManagerService managerService;

    @GetMapping("/users")
    public List<UserInfoResponse> getAllUsers() {
        return managerService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    public UserInfoResponse getUserInfo(@PathVariable Long userId) {
        return managerService.getUserInfo(userId);
    }

    @GetMapping("/tenants")
    public List<TenantInfoResponse> getAllTenants() {
        return managerService.getAllTenants();
    }

    @GetMapping("/tenants/{tenantId}")
    public TenantInfoResponse getTenantInfo(@PathVariable Long tenantId) {
        return managerService.getTenantInfo(tenantId);
    }
}