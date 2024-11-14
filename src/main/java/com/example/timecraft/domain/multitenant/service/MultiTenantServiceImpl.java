package com.example.timecraft.domain.multitenant.service;

import java.util.Collections;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.multitenant.persistence.TenantEntity;
import com.example.timecraft.domain.multitenant.persistence.TenantRepository;
import com.example.timecraft.domain.multitenant.util.MultiTenantUtils;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

@Service
public class MultiTenantServiceImpl implements MultiTenantService {
  private final DataSource dataSource;
  private final JdbcTemplate jdbcTemplate;
  @Qualifier("tenantLiquibaseProperties")
  private final LiquibaseProperties liquibaseProperties;
  private final ResourceLoader resourceLoader;
  private final TenantRepository tenantRepository;

  public MultiTenantServiceImpl(
      DataSource dataSource,
      JdbcTemplate jdbcTemplate,
      @Qualifier("tenantLiquibaseProperties") LiquibaseProperties liquibaseProperties,
      ResourceLoader resourceLoader,
      TenantRepository tenantRepository) {
    this.dataSource = dataSource;
    this.jdbcTemplate = jdbcTemplate;
    this.liquibaseProperties = liquibaseProperties;
    this.resourceLoader = resourceLoader;
    this.tenantRepository = tenantRepository;
  }

  @Override
  public TenantEntity createTenant(final String schemaName) {
    try {
      createSchema(schemaName);
      runLiquibase(dataSource, schemaName);
    } catch (LiquibaseException e) {
      throw new BadRequestException("Error while running schema creation : " + e.getMessage());
    }
    final TenantEntity tenant = TenantEntity.builder()
        .schemaName(schemaName)
        .build();
    return tenantRepository.save(tenant);
  }

  private void createSchema(String schema) {
    jdbcTemplate.execute("CREATE SCHEMA " + schema);
  }

  private void runLiquibase(DataSource dataSource, String schema) throws LiquibaseException {
    final SpringLiquibase liquibase = getSpringLiquibase(dataSource, schema);
    liquibase.afterPropertiesSet();
  }

  private SpringLiquibase getSpringLiquibase(DataSource dataSource, String schema) {
    final SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setResourceLoader(resourceLoader);
    liquibase.setDataSource(dataSource);
    liquibase.setDefaultSchema(schema);
    if (liquibaseProperties.getParameters() != null) {
      liquibaseProperties.getParameters().put("schema", schema);
      liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
    } else {
      liquibase.setChangeLogParameters(Collections.singletonMap("schema", schema));
    }
    MultiTenantUtils.setLiquibaseProperties(liquibase, liquibaseProperties);
    return liquibase;
  }
}
