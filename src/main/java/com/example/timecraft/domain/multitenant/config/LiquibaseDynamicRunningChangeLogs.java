package com.example.timecraft.domain.multitenant.config;

import java.util.Collection;
import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.example.timecraft.domain.multitenant.persistence.TenantEntity;
import com.example.timecraft.domain.multitenant.persistence.TenantRepository;
import com.example.timecraft.domain.multitenant.util.MultiTenantUtils;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class LiquibaseDynamicRunningChangeLogs implements InitializingBean, ResourceLoaderAware {
  private final TenantRepository tenantRepository;
  private final DataSource dataSource;
  @Qualifier("tenantLiquibaseProperties")
  private final LiquibaseProperties liquibaseProperties;
  private ResourceLoader resourceLoader;

  public LiquibaseDynamicRunningChangeLogs(
      DataSource dataSource,
      @Qualifier("tenantLiquibaseProperties") LiquibaseProperties liquibaseProperties,
      ResourceLoader resourceLoader,
      TenantRepository tenantRepository) {
    this.dataSource = dataSource;
    this.liquibaseProperties = liquibaseProperties;
    this.resourceLoader = resourceLoader;
    this.tenantRepository = tenantRepository;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.runOnAllSchemas(dataSource, tenantRepository.findAll());
  }

  protected void runOnAllSchemas(DataSource dataSource, Collection<TenantEntity> tenants) throws LiquibaseException {
    for (TenantEntity tenant : tenants) {
      SpringLiquibase liquibase = this.getSpringLiquibase(dataSource, tenant.getSchemaName());
      liquibase.afterPropertiesSet();
    }
  }

  protected SpringLiquibase getSpringLiquibase(DataSource dataSource, String schema) {
    return MultiTenantUtils.getSpringLiquibase(dataSource, schema, resourceLoader, liquibaseProperties);
  }

}
