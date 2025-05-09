package com.example.timecraft.domain.multitenant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import com.example.timecraft.domain.user.api.UserService;
import com.example.timecraft.domain.user.persistence.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantInterceptor implements WebRequestInterceptor {
  private final UserService userService;

  @Value("${multitenancy.tenant.default-tenant}")
  private String defaultTenant;

  @Override
  @Transactional
  public void preHandle(WebRequest request) {
    final String accessToken = request.getHeader("Authorization");
    String requestURI = null;
    if (request.getAttribute("org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping", 0) != null) {
      requestURI = request.getAttribute("org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping", 0).toString();
    }

    if (requestURI != null && (requestURI.contains("/admin") || requestURI.contains("/manager"))) {
      TenantIdentifierResolver.setCurrentTenant(defaultTenant);
      return;
    }
    if (accessToken != null) {
      final UserEntity currentUser = userService.findByAccessToken(accessToken);
      TenantIdentifierResolver.setCurrentTenant(currentUser.getTenants().getFirst().getSchemaName());
    } else {
      TenantIdentifierResolver.setCurrentTenant(defaultTenant);
    }
  }

  @Override
  public void postHandle(@NonNull WebRequest request, ModelMap model) {
  }

  @Override
  public void afterCompletion(@NonNull WebRequest request, Exception ex) {
    TenantIdentifierResolver.clear();
  }
}
