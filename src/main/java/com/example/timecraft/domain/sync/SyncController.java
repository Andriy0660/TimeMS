package com.example.timecraft.domain.sync;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.sync.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.service.SyncService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sync")
public class SyncController {
  private final SyncService syncService;

  @PostMapping("/syncFromJira")
  public void syncFromJira(@RequestBody SyncFromJiraRequest request) {
    syncService.syncFromJira(request);
  }

  @PostMapping("/syncIntoJira")
  public void syncIntoJira(@RequestBody final SyncIntoJiraRequest request) {
    syncService.syncIntoJira(request);
  }

}
