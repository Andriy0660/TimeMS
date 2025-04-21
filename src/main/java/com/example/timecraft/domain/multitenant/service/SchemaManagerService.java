package com.example.timecraft.domain.multitenant.service;

import com.example.timecraft.domain.multitenant.persistence.TenantEntity;

public interface SchemaManagerService {

    void initializeMainSchema();

    TenantEntity createTenantSchema(String schemaName);
}