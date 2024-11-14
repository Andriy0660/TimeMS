package com.example.timecraft.domain.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.auth.dto.AuthLogInRequest;
import com.example.timecraft.domain.auth.dto.AuthLogInResponse;
import com.example.timecraft.domain.auth.dto.AuthSignUpRequest;
import com.example.timecraft.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;

  @PostMapping("/signUp")
  public void signUp(@RequestBody AuthSignUpRequest request) {
    authService.signUp(request);
  }

  @PostMapping("/logIn")
  public AuthLogInResponse signIn(@RequestBody AuthLogInRequest request) {
    return authService.logIn(request);
  }

}
