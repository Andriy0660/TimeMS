package com.example.timecraft.domain.multitenant.service;

import java.util.List;
import javax.sql.DataSource;

import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.multitenant.persistence.TenantEntity;
import com.example.timecraft.domain.multitenant.persistence.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SchemaManagerServiceImpl implements SchemaManagerService {
    private final JdbcTemplate jdbcTemplate;
    private final TenantRepository tenantRepository;

    @Override
    public void initializeMainSchema() {
        log.info("Initializing main schema");
        try {
            jdbcTemplate.execute("SELECT initialize_main_schema()");
            log.info("Main schema initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing main schema", e);
            throw new BadRequestException("Error initializing main schema: " + e.getMessage());
        }
    }

    @Override
    public TenantEntity createTenantSchema(String schemaName) {
        log.info("Creating tenant schema: {}", schemaName);
        try {
            List<String> existingSchemas = jdbcTemplate.queryForList(
                "SELECT schema_name FROM information_schema.schemata WHERE schema_name = ?",
                String.class,
                schemaName
            );

            if (existingSchemas.isEmpty()) {
                jdbcTemplate.execute("SELECT create_tenant_schema('" + schemaName + "')");
                log.info("Tenant schema created successfully: {}", schemaName);
            } else {
                log.info("Tenant schema already exists: {}", schemaName);
            }

            TenantEntity tenant = TenantEntity.builder()
                .schemaName(schemaName)
                .build();

            return tenantRepository.save(tenant);
        } catch (Exception e) {
            log.error("Error creating tenant schema: {}", schemaName, e);
            throw new BadRequestException("Error creating tenant schema: " + e.getMessage());
        }
    }
}