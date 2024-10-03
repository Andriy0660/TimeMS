package com.example.timecraft.domain.logsync;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.logsync.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.logsync.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.logsync.service.LogSyncService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logsync")
public class LogSyncController {
  private final LogSyncService logSyncService;

  @PostMapping("/syncFromJira")
  public void syncFromJira(@RequestBody SyncFromJiraRequest request) {
    logSyncService.syncFromJira(request);
  }

  @PostMapping("/syncIntoJira")
  public void syncIntoJira(@RequestBody final SyncIntoJiraRequest request) {
    logSyncService.syncIntoJira(request);
  }

}
