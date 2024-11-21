package com.example.timecraft.domain.auth.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.auth.dto.AuthLogInRequest;
import com.example.timecraft.domain.auth.dto.AuthLogInResponse;
import com.example.timecraft.domain.auth.dto.AuthLogInWithGoogleRequest;
import com.example.timecraft.domain.auth.dto.AuthSignUpRequest;
import com.example.timecraft.domain.multitenant.persistence.TenantEntity;
import com.example.timecraft.domain.multitenant.service.MultiTenantService;
import com.example.timecraft.domain.multitenant.util.MultiTenantUtils;
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

  @Override
  public void signUp(final AuthSignUpRequest request) {
    if (userService.existsByEmail(request.getEmail())) {
      throw new BadRequestException("The email is already used");
    }
    final UserEntity userEntity = UserEntity.builder()
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .build();

    final TenantEntity defaultTenantForUser = multiTenantService.createTenant(generateSchemaNameFromEmail(userEntity.getEmail()));
    userEntity.getTenants().add(defaultTenantForUser);
    userService.save(userEntity);
  }

  @Override
  public AuthLogInResponse logIn(final AuthLogInRequest request) {
    final Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
        request.getEmail(), request.getPassword()
    ));
    final UserEntity user = (UserEntity) auth.getPrincipal();
    user.setAccessToken(UUID.randomUUID().toString());
    userService.save(user);
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
    user.setAccessToken(UUID.randomUUID().toString());
    userService.save(user);
    return new AuthLogInResponse(user.getAccessToken());
  }

  private void signUpWithGoogle(final String email, final String firstName, final String lastName) {
    final UserEntity userEntity = UserEntity.builder()
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .build();

    final TenantEntity defaultTenantForUser = multiTenantService.createTenant(generateSchemaNameFromEmail(userEntity.getEmail()));
    userEntity.getTenants().add(defaultTenantForUser);
    userService.save(userEntity);
  }


  @Override
  public void logOut() {
    final UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    user.setAccessToken(null);
    userService.save(user);
  }
}
