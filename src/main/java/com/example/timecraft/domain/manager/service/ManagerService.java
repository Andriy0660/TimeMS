package com.example.timecraft.domain.manager.service;

import java.util.List;

import com.example.timecraft.domain.manager.dto.TenantInfoResponse;
import com.example.timecraft.domain.manager.dto.UserInfoResponse;

public interface ManagerService {
    List<UserInfoResponse> getAllUsers();
    UserInfoResponse getUserInfo(Long userId);
    List<TenantInfoResponse> getAllTenants();
    TenantInfoResponse getTenantInfo(Long tenantId);
}