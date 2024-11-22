package com.example.timecraft.domain.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.config.dto.ConfigGetResponse;
import com.example.timecraft.domain.config.dto.ConfigUpdateExternalServiceRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateJiraRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateTimeRequest;
import com.example.timecraft.domain.config.service.ConfigService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/config")
public class ConfigController {
  private final ConfigService configService;

  @GetMapping
  public ConfigGetResponse getConfig() {
    return configService.getConfig();
  }

  @PatchMapping("/time")
  public void updateTimeConfig(@RequestBody final ConfigUpdateTimeRequest request) {
    configService.updateTimeConfig(request);
  }

  @PatchMapping("/jira")
  public void updateJiraConfig(@RequestBody final ConfigUpdateJiraRequest request) {
    configService.updateJiraConfig(request);
  }

  @PatchMapping("/externalService")
  public void updateExternalServiceConfig(@RequestBody final ConfigUpdateExternalServiceRequest request) {
    configService.updateExternalServiceConfig(request);
  }
}
