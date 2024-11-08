package com.example.timecraft.core.config;

import java.util.Collections;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.core.exception.UnauthorizedException;
import com.example.timecraft.domain.user.api.UserService;
import com.example.timecraft.domain.user.persistence.UserEntity;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthProvider implements AuthenticationProvider {
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String email = (String) authentication.getPrincipal();
    String password = (String) authentication.getCredentials();
    UserEntity user;
    try {
      user = userService.findByEmail(email);
    } catch (NotFoundException e) {
      throw new UnauthorizedException("User with email " + email + " not found");
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new UnauthorizedException("Invalid password!");
    }
    Set<GrantedAuthority> authorities = Collections.emptySet();
    return new UsernamePasswordAuthenticationToken(user, null, authorities);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
  }
}

