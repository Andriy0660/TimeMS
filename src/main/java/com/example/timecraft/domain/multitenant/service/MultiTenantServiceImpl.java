package com.example.timecraft.domain.multitenant.service;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.multitenant.persistence.TenantEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MultiTenantServiceImpl implements MultiTenantService {
  private final SchemaManagerService schemaManagerService;

  @Override
  public TenantEntity createTenant(final String schemaName) {
    return schemaManagerService.createTenantSchema(schemaName);
  }
}