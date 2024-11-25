package com.example.timecraft.domain.external_timelog;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogRequest;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogResponse;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogListResponse;
import com.example.timecraft.domain.external_timelog.service.ExternalTimeLogService;
import com.example.timecraft.domain.sync.external_service.api.SyncExternalServiceProcessingService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/external-time-logs")
public class ExternalTimeLogController {

  private final ExternalTimeLogService externalTimeLogService;
  private final SyncExternalServiceProcessingService syncExternalServiceProcessingService;

  @GetMapping()
  public ExternalTimeLogListResponse list(@RequestParam final LocalDate date) {
    return syncExternalServiceProcessingService.processExternalTimeLogDtos(externalTimeLogService.list(date));
  }

  @PostMapping
  public ExternalTimeLogCreateFromTimeLogResponse createFromTimeLog(@RequestBody final ExternalTimeLogCreateFromTimeLogRequest request) {
    return externalTimeLogService.createFromTimeLog(request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable final Long id) {
    externalTimeLogService.delete(id);
  }

}