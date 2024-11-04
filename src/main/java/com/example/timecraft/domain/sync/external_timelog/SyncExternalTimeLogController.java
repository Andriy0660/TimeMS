package com.example.timecraft.domain.sync.external_timelog;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.sync.external_timelog.dto.SyncIntoExternalServiceRequest;
import com.example.timecraft.domain.sync.external_timelog.service.SyncExternalTimeLogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/syncExternalService")
public class SyncExternalTimeLogController {
  private final SyncExternalTimeLogService syncExternalTimeLogService;

  @PostMapping("/to")
  public void syncIntoExternalService(@RequestBody final SyncIntoExternalServiceRequest request) {
    syncExternalTimeLogService.syncIntoExternalService(request);
  }
}
