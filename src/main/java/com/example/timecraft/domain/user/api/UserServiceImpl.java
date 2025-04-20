package com.example.timecraft.domain.user.api;

import org.springframework.stereotype.Service;

import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.user.persistence.UserEntity;
import com.example.timecraft.domain.user.persistence.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository repository;

  @Override
  public UserEntity findByAccessToken(final String accessToken) {
    return repository.findByAccessToken(accessToken).orElseThrow(() -> new NotFoundException("User not found"));
  }

  @Override
  public UserEntity findByEmail(final String email) {
    return repository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
  }

  @Override
  public UserEntity save(final UserEntity user) {
    return repository.save(user);
  }

  @Override
  public boolean existsByEmail(final String email) {
    return repository.existsUserEntityByEmail(email);
  }
}
