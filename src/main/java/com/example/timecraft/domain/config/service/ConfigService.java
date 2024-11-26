package com.example.timecraft.domain.config.service;

import com.example.timecraft.domain.config.dto.ConfigGetResponse;
import com.example.timecraft.domain.config.dto.ConfigUpdateExternalServiceRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateJiraRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateTimeRequest;

public interface ConfigService {
  ConfigGetResponse getConfig();
  void updateTimeConfig(final ConfigUpdateTimeRequest request);
  void updateJiraConfig(final ConfigUpdateJiraRequest request);
  void updateExternalServiceConfig(final ConfigUpdateExternalServiceRequest request);
}
