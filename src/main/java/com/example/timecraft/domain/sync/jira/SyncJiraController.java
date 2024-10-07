package com.example.timecraft.domain.sync.jira;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.jira.service.SyncJiraApiService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sync")
public class SyncJiraController {
  private final SyncJiraApiService syncJiraApiService;

  @PostMapping("/syncFromJira")
  public void syncFromJira(@RequestBody SyncFromJiraRequest request) {
    syncJiraApiService.syncFromJira(request);
  }

  @PostMapping("/syncIntoJira")
  public void syncIntoJira(@RequestBody final SyncIntoJiraRequest request) {
    syncJiraApiService.syncIntoJira(request);
  }

}
