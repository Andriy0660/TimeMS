package com.example.timecraft.domain.multitenant.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {
  private final DataSource dataSource;

  @Value("${multitenancy.tenant.default-tenant}")
  private String defaultTenant;

  @Override
  public Connection getAnyConnection() throws SQLException {
    return getConnection(defaultTenant);
  }

  @Override
  public void releaseAnyConnection(final Connection connection) throws SQLException {
    connection.close();
  }

  @Override
  public Connection getConnection(final String schemaName) throws SQLException {
    Connection connection = dataSource.getConnection();
    connection.setSchema(schemaName);
    return connection;
  }

  @Override
  public void releaseConnection(final String schemaName, final Connection connection) throws SQLException {
    connection.setSchema(defaultTenant);
    connection.close();
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false;
  }

  @Override
  public boolean isUnwrappableAs(Class unwrapType) {
    return MultiTenantConnectionProvider.class.isAssignableFrom(unwrapType);
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    if (MultiTenantConnectionProvider.class.isAssignableFrom(unwrapType)) {
      return (T) this;
    } else {
      throw new UnknownUnwrapTypeException(unwrapType);
    }
  }

  @Override
  public void customize(final Map<String, Object> hibernateProperties) {
    hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
  }
}