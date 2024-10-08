package com.example.timecraft.domain.timelog;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.sync.jira.service.SyncJiraProcessingService;
import com.example.timecraft.domain.timelog.dto.TimeLogChangeDateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogConfigResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFormWorklogResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFromWorklogRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogImportRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.service.TimeLogApiService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/time-logs")
public class TimeLogController {
  private final TimeLogApiService timeLogApiService;
  private final SyncJiraProcessingService syncJiraProcessingService;

  @GetMapping
  public TimeLogListResponse list(@RequestParam final String mode, @RequestParam final LocalDate date) {
    return syncJiraProcessingService.processTimeLogDtos(timeLogApiService.list(mode, date));
  }

  @PostMapping
  public TimeLogCreateResponse create(@RequestBody final TimeLogCreateRequest request) {
    return timeLogApiService.create(request);
  }

  @PostMapping("/fromWorklog")
  public TimeLogCreateFormWorklogResponse createFromWorklog(@RequestBody final TimeLogCreateFromWorklogRequest request) {
    return timeLogApiService.createFromWorklog(request);
  }

  @PostMapping("/importTimeLogs")
  public void importTimeLogs(@RequestBody final TimeLogImportRequest request) {
    timeLogApiService.importTimeLogs(request);
  }

  @PostMapping("/divide/{timeLogId}")
  public void divide(@PathVariable final long timeLogId) {
    timeLogApiService.divide(timeLogId);
  }

  @GetMapping("/{timeLogId}")
  public TimeLogGetResponse get(@PathVariable final long timeLogId) {
    return timeLogApiService.get(timeLogId);
  }

  @GetMapping("/config")
  public TimeLogConfigResponse getOffset() {
    return timeLogApiService.getConfig();
  }

  @GetMapping("/hoursForWeek")
  public TimeLogHoursForWeekResponse getHoursForWeek(@RequestParam final LocalDate date) {
    return syncJiraProcessingService.processWeekDayInfos(timeLogApiService.getHoursForWeek(date));
  }

  @GetMapping("/hoursForMonth")
  public TimeLogHoursForMonthResponse getHoursForMonth(@RequestParam final LocalDate date) {
    return syncJiraProcessingService.processMonthDayInfos(timeLogApiService.getHoursForMonth(date));
  }

  @PutMapping("/{timeLogId}")
  public TimeLogUpdateResponse update(@PathVariable final long timeLogId, @RequestBody final TimeLogUpdateRequest request) {
    return timeLogApiService.update(timeLogId, request);
  }

  @DeleteMapping("/{timeLogId}")
  public void delete(@PathVariable final long timeLogId) {
    timeLogApiService.delete(timeLogId);
  }

  @PatchMapping("/setGroupDescription")
  public void setGroupDescription(@RequestBody final TimeLogSetGroupDescrRequest request) {
    timeLogApiService.setGroupDescription(request);
  }

  @PatchMapping("/{timeLogId}/changeDate")
  public void changeDate(@PathVariable final long timeLogId, @RequestBody final TimeLogChangeDateRequest request) {
    timeLogApiService.changeDate(timeLogId, request);
  }

}
