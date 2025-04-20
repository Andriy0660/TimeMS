package com.example.timecraft.domain.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.audit.service.AuditService;
import com.example.timecraft.domain.auth.dto.AuthLogInRequest;
import com.example.timecraft.domain.auth.dto.AuthLogInResponse;
import com.example.timecraft.domain.auth.dto.AuthLogInWithGoogleRequest;
import com.example.timecraft.domain.auth.dto.AuthSignUpRequest;
import com.example.timecraft.domain.multitenant.config.TenantIdentifierResolver;
import com.example.timecraft.domain.multitenant.persistence.TenantEntity;
import com.example.timecraft.domain.multitenant.service.MultiTenantService;
import com.example.timecraft.domain.multitenant.util.MultiTenantUtils;
import com.example.timecraft.domain.role.persistence.RoleEntity;
import com.example.timecraft.domain.role.persistence.RoleRepository;
import com.example.timecraft.domain.user.api.UserService;
import com.example.timecraft.domain.user.persistence.UserEntity;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import static com.example.timecraft.domain.multitenant.util.MultiTenantUtils.generateSchemaNameFromEmail;
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
  private final UserService userService;
  private final MultiTenantService multiTenantService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final GoogleIdTokenVerifier verifier;
  private final RoleRepository roleRepository;
  private final AuditService auditService;

  @Override
  public void signUp(final AuthSignUpRequest request) {
    if (userService.existsByEmail(request.getEmail())) {
      throw new BadRequestException("The email is already used");
    }

    // Отримуємо роль USER
    RoleEntity userRole = roleRepository.findByName("ROLE_USER")
        .orElseThrow(() -> new RuntimeException("Default role not found"));

    final UserEntity userEntity = UserEntity.builder()
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .isActive(true)
        .roles(List.of(userRole))
        .build();

    final TenantEntity defaultTenantForUser = multiTenantService.createTenant(generateSchemaNameFromEmail(userEntity.getEmail()));
    userEntity.getTenants().add(defaultTenantForUser);

    UserEntity savedUser = userService.save(userEntity);
    TenantIdentifierResolver.setCurrentTenant("public");

    // Логуємо реєстрацію
    auditService.logAction(
        savedUser,
        "USER_REGISTRATION",
        "USER",
        savedUser.getId().toString(),
        "User registered: " + savedUser.getEmail(),
        defaultTenantForUser.getId()
    );
  }

  @Override
  public AuthLogInResponse logIn(final AuthLogInRequest request) {
    final Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
        request.getEmail(), request.getPassword()
    ));
    final UserEntity user = (UserEntity) auth.getPrincipal();
    user.setAccessToken(UUID.randomUUID().toString());
    UserEntity savedUser = userService.save(user);

    // Логуємо вхід
    Long tenantId = null;
    if (!savedUser.getTenants().isEmpty()) {
        tenantId = savedUser.getTenants().getFirst().getId();
    }

    auditService.logAction(
        savedUser,
        "USER_LOGIN",
        "USER",
        savedUser.getId().toString(),
        "User logged in: " + savedUser.getEmail(),
        tenantId
    );

    return new AuthLogInResponse(user.getAccessToken());
  }

  @Override
  public AuthLogInResponse logInWithGoogle(final AuthLogInWithGoogleRequest request) {
    final GoogleIdToken idToken;
    try {
      idToken = verifier.verify(request.getCredential());
    } catch (Exception e) {
      throw new BadRequestException("Invalid credentials");
    }
    if (idToken == null) {
      throw new BadRequestException("Invalid credentials");
    }
    final GoogleIdToken.Payload payload = idToken.getPayload();

    final String email = payload.getEmail();
    final String firstName = (String) payload.get("given_name");
    final String lastName = (String) payload.get("family_name");

    if (!userService.existsByEmail(email)) {
      signUpWithGoogle(email, firstName, lastName);
    }

    final UserEntity user = userService.findByEmail(email);

    // Перевірка активності користувача
    if (!user.isActive()) {
      throw new BadRequestException("User account is disabled");
    }

    user.setAccessToken(UUID.randomUUID().toString());
    UserEntity savedUser = userService.save(user);

    // Логуємо вхід через Google
    Long tenantId = null;
    if (!savedUser.getTenants().isEmpty()) {
        tenantId = savedUser.getTenants().getFirst().getId();
    }

    auditService.logAction(
        savedUser,
        "USER_LOGIN_GOOGLE",
        "USER",
        savedUser.getId().toString(),
        "User logged in via Google: " + savedUser.getEmail(),
        tenantId
    );

    return new AuthLogInResponse(user.getAccessToken());
  }

  private void signUpWithGoogle(final String email, final String firstName, final String lastName) {
    // Отримуємо роль USER
    RoleEntity userRole = roleRepository.findByName("ROLE_USER")
        .orElseThrow(() -> new RuntimeException("Default role not found"));

    final UserEntity userEntity = UserEntity.builder()
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .isActive(true)
        .roles(new ArrayList<>(List.of(userRole)))
        .build();

    final TenantEntity defaultTenantForUser = multiTenantService.createTenant(generateSchemaNameFromEmail(userEntity.getEmail()));
    userEntity.getTenants().add(defaultTenantForUser);

    UserEntity savedUser = userService.save(userEntity);

    // Логуємо реєстрацію через Google
    auditService.logAction(
        savedUser,
        "USER_REGISTRATION_GOOGLE",
        "USER",
        savedUser.getId().toString(),
        "User registered via Google: " + savedUser.getEmail(),
        defaultTenantForUser.getId()
    );
  }

  @Override
  public void logOut() {
    final UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    user.setAccessToken(null);
    UserEntity savedUser = userService.save(user);

    // Логуємо вихід
    Long tenantId = null;
    if (!savedUser.getTenants().isEmpty()) {
        tenantId = savedUser.getTenants().getFirst().getId();
    }

    auditService.logAction(
        savedUser,
        "USER_LOGOUT",
        "USER",
        savedUser.getId().toString(),
        "User logged out: " + savedUser.getEmail(),
        tenantId
    );
  }
}
