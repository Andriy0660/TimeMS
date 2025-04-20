package com.example.timecraft.domain.auth;

import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.auth.dto.AuthLogInRequest;
import com.example.timecraft.domain.auth.dto.AuthLogInResponse;
import com.example.timecraft.domain.auth.dto.AuthLogInWithGoogleRequest;
import com.example.timecraft.domain.auth.dto.AuthSignUpRequest;
import com.example.timecraft.domain.auth.service.AuthService;
import com.example.timecraft.domain.manager.dto.UserInfoResponse;
import com.example.timecraft.domain.user.persistence.UserEntity;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;

  @PostMapping("/signup")
  public void signUp(@RequestBody final AuthSignUpRequest request) {
    authService.signUp(request);
  }

  @PostMapping("/login")
  public AuthLogInResponse logIn(@RequestBody final AuthLogInRequest request) {
    return authService.logIn(request);
  }

  @PostMapping("/google/login")
  public AuthLogInResponse logInWithGoogle(@RequestBody final AuthLogInWithGoogleRequest request) {
    return authService.logInWithGoogle(request);
  }

  @PostMapping("/logout")
  public void logOut() {
    authService.logOut();
  }

  @GetMapping("/me")
  public UserInfoResponse getCurrentUser() {
    UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    return UserInfoResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .isActive(user.isActive())
        .roles(user.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toList()))
        .build();
  }

}
