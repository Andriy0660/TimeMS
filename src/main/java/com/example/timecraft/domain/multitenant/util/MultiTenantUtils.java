package com.example.timecraft.domain.multitenant.util;

import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

import liquibase.integration.spring.SpringLiquibase;

public class MultiTenantUtils {
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
}
