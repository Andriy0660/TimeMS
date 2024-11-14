package com.example.timecraft.domain.multitenant.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.timecraft.domain.multitenant.util.MultiTenantUtils;
import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseConfig {
  @Bean
  @ConfigurationProperties("multitenancy.tenant.liquibase")
  public LiquibaseProperties tenantLiquibaseProperties() {
    return new LiquibaseProperties();
  }

  @Bean
  @ConfigurationProperties("multitenancy.main.liquibase")
  public LiquibaseProperties masterLiquibaseProperties() {
    return new LiquibaseProperties();
  }

  @Bean
  public SpringLiquibase liquibase(ObjectProvider<DataSource> liquibaseDataSource) {
    final LiquibaseProperties liquibaseProperties = masterLiquibaseProperties();
    final SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(liquibaseDataSource.getIfAvailable());
    MultiTenantUtils.setLiquibaseProperties(liquibase, liquibaseProperties);
    return liquibase;
  }
}
