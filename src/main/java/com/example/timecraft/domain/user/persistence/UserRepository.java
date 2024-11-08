package com.example.timecraft.domain.user.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByAccessToken(String accessToken);

  Optional<UserEntity> findByEmail(String email);

  boolean existsUserEntityByEmail(String email);
}
