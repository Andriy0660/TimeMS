package com.example.timecraft.domain.multitenant.util;

import java.util.Collections;
import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.core.io.ResourceLoader;

import liquibase.integration.spring.SpringLiquibase;

public class MultiTenantUtils {
  public static SpringLiquibase getSpringLiquibase(final DataSource dataSource, final String schema, final ResourceLoader resourceLoader, final LiquibaseProperties liquibaseProperties) {
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

  public static void setLiquibaseProperties(final SpringLiquibase liquibase, final LiquibaseProperties liquibaseProperties) {
    liquibase.setChangeLog(liquibaseProperties.getChangeLog());
    liquibase.setContexts(liquibaseProperties.getContexts());
    liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
    liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
    liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
    liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
    liquibase.setDropFirst(liquibaseProperties.isDropFirst());
    liquibase.setShouldRun(liquibaseProperties.isEnabled());
    liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
    liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
  }

  public static String generateSchemaNameFromEmail(String email) {
    String schemaName = email.toLowerCase();
    schemaName = schemaName.replaceAll("[^a-z0-9_]", "_");
    if (schemaName.length() > 63) {
      schemaName = schemaName.substring(0, 63);
    }
    return schemaName;
  }
}
