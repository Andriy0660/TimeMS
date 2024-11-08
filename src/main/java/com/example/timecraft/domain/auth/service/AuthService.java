package com.example.timecraft.domain.auth.service;

import com.example.timecraft.domain.auth.dto.AuthLogInRequest;
import com.example.timecraft.domain.auth.dto.AuthLogInResponse;
import com.example.timecraft.domain.auth.dto.AuthSignUpRequest;

public interface AuthService {
  void signUp(final AuthSignUpRequest request);

  AuthLogInResponse logIn(final AuthLogInRequest request);
}
