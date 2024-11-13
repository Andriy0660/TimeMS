package com.example.timecraft.domain.multitenant.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    LiquibaseProperties liquibaseProperties = masterLiquibaseProperties();
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(liquibaseDataSource.getIfAvailable());
    liquibase.setChangeLog(liquibaseProperties.getChangeLog());
    liquibase.setContexts(liquibaseProperties.getContexts());
    liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
    liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
    liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
    liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
    liquibase.setDropFirst(liquibaseProperties.isDropFirst());
    liquibase.setShouldRun(liquibaseProperties.isEnabled());
    liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
    liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
    liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
    return liquibase;
  }
}
