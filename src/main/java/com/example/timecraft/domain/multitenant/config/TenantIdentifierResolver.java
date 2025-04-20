package com.example.timecraft.domain.multitenant.config;

import java.util.Map;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {
  private static final ThreadLocal<String> currentTenant = ThreadLocal.withInitial(() -> "public");

  public static void setCurrentTenant(String tenant) {
    currentTenant.set(tenant);
  }

  public static String getCurrentTenant() {
    return currentTenant.get();
  }

  public static void clear() {
    currentTenant.remove();
  }

  @Override
  public String resolveCurrentTenantIdentifier() {
    return currentTenant.get();
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return false;
  }

  @Override
  public boolean isRoot(final String schemaName) {
    return CurrentTenantIdentifierResolver.super.isRoot(schemaName);
  }

  @Override
  public void customize(Map<String, Object> hibernateProperties) {
    hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
  }
}
