package com.example.timecraft.domain.admin;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.admin.dto.RoleResponse;
import com.example.timecraft.domain.admin.service.RoleService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/roles")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping("/user/{userId}/add")
    public void addRoleToUser(@PathVariable Long userId, @RequestParam String roleName) {
        roleService.addRoleToUser(userId, roleName);
    }

    @PostMapping("/user/{userId}/remove")
    public void removeRoleFromUser(@PathVariable Long userId, @RequestParam String roleName) {
        roleService.removeRoleFromUser(userId, roleName);
    }
}