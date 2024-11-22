package com.example.timecraft.domain.config.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.domain.config.dto.ConfigGetResponse;
import com.example.timecraft.domain.config.dto.ConfigUpdateExternalServiceRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateJiraRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateTimeRequest;
import com.example.timecraft.domain.config.mapper.ConfigMapper;
import com.example.timecraft.domain.config.persistence.ConfigEntity;
import com.example.timecraft.domain.config.persistence.ConfigRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
  private final ConfigRepository configRepository;
  private final ConfigMapper mapper;

  @Override
  public ConfigGetResponse getConfig() {
    final ConfigEntity config = configRepository.findAll().getFirst();
    return mapper.fromEntity(config);
  }

  @Override
  public void updateTimeConfig(final ConfigUpdateTimeRequest request) {
    final ConfigEntity config = configRepository.findAll().getFirst();
    config.setDayOffsetHour(request.getDayOffsetHour());
    config.setWorkingDayStartHour(request.getWorkingDayStartHour());
    config.setWorkingDayEndHour(request.getWorkingDayEndHour());
    configRepository.save(config);
  }

  @Override
  public void updateJiraConfig(final ConfigUpdateJiraRequest request) {
    final ConfigEntity config = configRepository.findAll().getFirst();
    config.setIsJiraEnabled(request.getIsJiraEnabled());
    configRepository.save(config);
  }

  @Override
  public void updateExternalServiceConfig(final ConfigUpdateExternalServiceRequest request) {
    final ConfigEntity config = configRepository.findAll().getFirst();
    config.setIsExternalServiceEnabled(request.getIsExternalServiceEnabled());
    config.setExternalServiceTimeCf(request.getExternalServiceTimeCf());
    config.setIsExternalServiceIncludeDescription(request.getIsExternalServiceIncludeDescription());
    configRepository.save(config);
  }
}
