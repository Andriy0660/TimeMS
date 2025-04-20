package com.example.timecraft.domain.admin.service;

import java.util.List;

import com.example.timecraft.domain.admin.dto.RoleResponse;

public interface RoleService {
    List<RoleResponse> getAllRoles();
    void addRoleToUser(Long userId, String roleName);
    void removeRoleFromUser(Long userId, String roleName);
}