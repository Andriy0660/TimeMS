package com.example.timecraft.domain.user.api;

import com.example.timecraft.domain.user.persistence.UserEntity;

public interface UserService {
  UserEntity findByAccessToken(final String accessToken);

  UserEntity findByEmail(final String email);

  void save(final UserEntity user);

  boolean existsByEmail(final String email);
}
