package com.example.timecraft.domain.config.api;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.config.persistence.ConfigRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserConfigServiceImpl implements UserConfigService {
  private final ConfigRepository configRepository;

  @Override
  public int getOffsetHour() {
    return configRepository.findAll().getFirst().getDayOffsetHour();
  }

  @Override
  public boolean getIsExternalServiceIncludeDescription() {
    return configRepository.findAll().getFirst().getIsExternalServiceIncludeDescription();
  }
}
