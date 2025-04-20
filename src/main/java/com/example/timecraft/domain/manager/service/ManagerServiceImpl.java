package com.example.timecraft.domain.manager.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.audit.service.AuditService;
import com.example.timecraft.domain.manager.dto.TenantInfoResponse;
import com.example.timecraft.domain.manager.dto.UserInfoResponse;
import com.example.timecraft.domain.multitenant.persistence.TenantEntity;
import com.example.timecraft.domain.multitenant.persistence.TenantRepository;
import com.example.timecraft.domain.user.persistence.UserEntity;
import com.example.timecraft.domain.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {
   private final UserRepository userRepository;
   private final TenantRepository tenantRepository;
   private final AuditService auditService;

   @Override
   public List<UserInfoResponse> getAllUsers() {
       List<UserEntity> users = userRepository.findAll();
       List<UserInfoResponse> response = users.stream()
           .map(this::mapToUserInfoResponse)
           .collect(Collectors.toList());

       auditService.logAction("VIEW_ALL_USERS", "USER", "ALL", "Viewed all users");

       return response;
   }

   @Override
   public UserInfoResponse getUserInfo(Long userId) {
       UserEntity user = userRepository.findById(userId)
           .orElseThrow(() -> new NotFoundException("User not found"));

       UserInfoResponse response = mapToUserInfoResponse(user);

       auditService.logAction(
           "VIEW_USER",
           "USER",
           userId.toString(),
           "Viewed user: " + user.getEmail()
       );

       return response;
   }

   @Override
   public List<TenantInfoResponse> getAllTenants() {
       List<TenantEntity> tenants = tenantRepository.findAll();
       List<TenantInfoResponse> response = tenants.stream()
           .map(this::mapToTenantInfoResponse)
           .collect(Collectors.toList());

       auditService.logAction("VIEW_ALL_TENANTS", "TENANT", "ALL", "Viewed all tenants");

       return response;
   }

   @Override
   public TenantInfoResponse getTenantInfo(Long tenantId) {
       TenantEntity tenant = tenantRepository.findById(tenantId)
           .orElseThrow(() -> new NotFoundException("Tenant not found"));

       TenantInfoResponse response = mapToTenantInfoResponse(tenant);

       auditService.logAction(
           "VIEW_TENANT",
           "TENANT",
           tenantId.toString(),
           "Viewed tenant: " + tenant.getSchemaName()
       );

       return response;
   }

   private UserInfoResponse mapToUserInfoResponse(UserEntity user) {
       List<TenantInfoResponse> tenantResponses = user.getTenants().stream()
           .map(this::mapToTenantInfoResponse)
           .collect(Collectors.toList());

       List<String> roles = user.getRoles().stream()
           .map(role -> role.getName())
           .collect(Collectors.toList());

       return UserInfoResponse.builder()
           .id(user.getId())
           .email(user.getEmail())
           .firstName(user.getFirstName())
           .lastName(user.getLastName())
           .isActive(user.isActive())
           .roles(roles)
           .tenants(tenantResponses)
           .build();
   }

   private TenantInfoResponse mapToTenantInfoResponse(TenantEntity tenant) {
       // Знаходимо користувача, якому належить тенант (для спрощення беремо першого)
       UserEntity owner = userRepository.findAll().stream()
           .filter(user -> user.getTenants().contains(tenant))
           .findFirst()
           .orElse(null);

       return TenantInfoResponse.builder()
           .id(tenant.getId())
           .schemaName(tenant.getSchemaName())
           .userId(owner != null ? owner.getId() : null)
           .userEmail(owner != null ? owner.getEmail() : null)
           .build();
   }
}