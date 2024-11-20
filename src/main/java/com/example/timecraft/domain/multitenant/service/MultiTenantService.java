package com.example.timecraft.domain.multitenant.service;

import com.example.timecraft.domain.multitenant.persistence.TenantEntity;

public interface MultiTenantService {
  TenantEntity createTenant(String tenantName);
}
