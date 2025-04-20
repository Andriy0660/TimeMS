package com.example.timecraft.domain.admin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.admin.dto.RoleResponse;
import com.example.timecraft.domain.audit.service.AuditService;
import com.example.timecraft.domain.role.persistence.RoleEntity;
import com.example.timecraft.domain.role.persistence.RoleRepository;
import com.example.timecraft.domain.user.persistence.UserEntity;
import com.example.timecraft.domain.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
            .map(role -> RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        RoleEntity role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        user.setRoles(new ArrayList<>(List.of(role)));

        userRepository.save(user);

        auditService.logAction(
            "ROLE_CHANGED",
            "USER",
            userId.toString(),
            "Role " + roleName + " changed to user: " + user.getEmail()
        );
    }

    @Override
    public void removeRoleFromUser(Long userId, String roleName) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        RoleEntity role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        // Не дозволяємо видалити останню роль
        if (user.getRoles().size() <= 1) {
            throw new BadRequestException("Cannot remove the last role from user");
        }

        boolean removed = user.getRoles().removeIf(r -> r.getName().equals(roleName));

        if (!removed) {
            throw new BadRequestException("User does not have this role");
        }

        userRepository.save(user);

        auditService.logAction(
            "ROLE_REMOVED",
            "USER",
            userId.toString(),
            "Role " + roleName + " removed from user: " + user.getEmail()
        );
    }
}