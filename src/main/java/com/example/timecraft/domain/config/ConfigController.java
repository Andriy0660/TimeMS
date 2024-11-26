package com.example.timecraft.domain.config;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.config.dto.ConfigGetResponse;
import com.example.timecraft.domain.config.dto.ConfigUpdateExternalServiceRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateJiraRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateTimeRequest;
import com.example.timecraft.domain.config.service.ConfigService;
import com.example.timecraft.domain.jira_instance.dto.JiraInstanceGetResponse;
import com.example.timecraft.domain.jira_instance.dto.JiraInstanceSaveRequest;
import com.example.timecraft.domain.jira_instance.service.JiraInstanceService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/config")
public class ConfigController {
  private final ConfigService configService;
  private final JiraInstanceService jiraInstanceService;

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

  @GetMapping("/jira/instance")
  public JiraInstanceGetResponse get() {
    return jiraInstanceService.get();
  }

  @PostMapping("/jira/instance")
  public void save(@RequestBody final JiraInstanceSaveRequest request) {
    jiraInstanceService.save(request);
  }

  @DeleteMapping("/jira/instance/{id}")
  public void delete(@PathVariable final Long id) {
    final boolean enableSyncWithJira = false;
    configService.updateJiraConfig(new ConfigUpdateJiraRequest(enableSyncWithJira));
    jiraInstanceService.delete(id);
  }

  @PatchMapping("/external-service")
  public void updateExternalServiceConfig(@RequestBody final ConfigUpdateExternalServiceRequest request) {
    configService.updateExternalServiceConfig(request);
  }
}
