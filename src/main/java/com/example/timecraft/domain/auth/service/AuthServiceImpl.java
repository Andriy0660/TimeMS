package com.example.timecraft.domain.auth.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.auth.dto.AuthLogInRequest;
import com.example.timecraft.domain.auth.dto.AuthLogInResponse;
import com.example.timecraft.domain.auth.dto.AuthSignUpRequest;
import com.example.timecraft.domain.user.api.UserService;
import com.example.timecraft.domain.user.persistence.UserEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  @Override
  public void signUp(final AuthSignUpRequest request) {
    if (userService.existsByEmail(request.getEmail())) {
      throw new BadRequestException("The email is already used");
    }
    UserEntity userEntity = UserEntity.builder()
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .build();

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
}
