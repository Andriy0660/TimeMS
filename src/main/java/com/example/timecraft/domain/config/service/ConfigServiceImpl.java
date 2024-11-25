package com.example.timecraft.domain.config.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.config.dto.ConfigGetResponse;
import com.example.timecraft.domain.config.dto.ConfigUpdateExternalServiceRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateJiraRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateTimeRequest;
import com.example.timecraft.domain.config.mapper.ConfigMapper;
import com.example.timecraft.domain.config.persistence.ConfigEntity;
import com.example.timecraft.domain.config.persistence.ConfigRepository;
import com.example.timecraft.domain.jira_instance.api.UserJiraInstanceService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
  private final ConfigRepository configRepository;
  private final ConfigMapper mapper;
  private final UserJiraInstanceService userJiraInstanceService;

  @Override
  public ConfigGetResponse getConfig() {
    final ConfigEntity config = configRepository.findAll().getFirst();
    return mapper.fromEntity(config);
  }

  @Override
  public void updateTimeConfig(final ConfigUpdateTimeRequest request) {
    validateTimeConfig(request);
    final ConfigEntity config = configRepository.findAll().getFirst();
    config.setDayOffsetHour(request.getDayOffsetHour());
    config.setWorkingDayStartHour(request.getWorkingDayStartHour());
    config.setWorkingDayEndHour(request.getWorkingDayEndHour());
    configRepository.save(config);
  }

  private void validateTimeConfig(final ConfigUpdateTimeRequest request) {
    if (request.getDayOffsetHour() < 0 || request.getDayOffsetHour() > 12) {
      throw new BadRequestException("Invalid day offset hour");
    }
    if (request.getWorkingDayStartHour() < 0 || request.getWorkingDayStartHour() > 12) {
      throw new BadRequestException("Invalid working day start hour");
    }
    if (request.getWorkingDayEndHour() < 12 || request.getWorkingDayEndHour() > 23) {
      throw new BadRequestException("Invalid working day end hour");
    }
  }

  @Override
  public void updateJiraConfig(final ConfigUpdateJiraRequest request) {
    if (request.getIsJiraEnabled() && userJiraInstanceService.getJiraInstance() == null) {
      throw new BadRequestException("Set up Jira Instance firstly");
    }

    final ConfigEntity config = configRepository.findAll().getFirst();
    config.setIsJiraEnabled(request.getIsJiraEnabled());
    configRepository.save(config);
  }

  @Override
  public void updateExternalServiceConfig(final ConfigUpdateExternalServiceRequest request) {
    validateExternalServiceConfig(request);
    final ConfigEntity config = configRepository.findAll().getFirst();
    config.setIsExternalServiceEnabled(request.getIsExternalServiceEnabled());
    config.setExternalServiceTimeCf(request.getExternalServiceTimeCf());
    config.setIsExternalServiceIncludeDescription(request.getIsExternalServiceIncludeDescription());
    configRepository.save(config);
  }

  private void validateExternalServiceConfig(final ConfigUpdateExternalServiceRequest request) {
    if (request.getExternalServiceTimeCf() <= 0) {
      throw new BadRequestException("Invalid external service time coefficient");
    }
  }
}
