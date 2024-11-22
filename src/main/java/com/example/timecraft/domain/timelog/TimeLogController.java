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

import com.example.timecraft.domain.sync.external_service.api.SyncExternalServiceProcessingService;
import com.example.timecraft.domain.sync.jira.api.SyncJiraProcessingService;
import com.example.timecraft.domain.timelog.dto.TimeLogChangeDateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFormWorklogResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFromWorklogRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogImportRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogWeekResponse;
import com.example.timecraft.domain.timelog.service.TimeLogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/time-logs")
public class TimeLogController {
  private final TimeLogService timeLogService;
  private final SyncJiraProcessingService syncJiraProcessingService;
  private final SyncExternalServiceProcessingService syncExternalServiceProcessingService;

  @GetMapping
  public TimeLogListResponse list(@RequestParam final LocalDate startDate, @RequestParam final LocalDate endDate) {
    return syncExternalServiceProcessingService.processTimeLogDtos(
        syncJiraProcessingService.processTimeLogDtos(
            timeLogService.list(startDate, endDate)
        )
    );
  }

  @PostMapping
  public TimeLogCreateResponse create(@RequestBody final TimeLogCreateRequest request) {
    return timeLogService.create(request);
  }

  @PostMapping("/fromWorklog")
  public TimeLogCreateFormWorklogResponse createFromWorklog(@RequestBody final TimeLogCreateFromWorklogRequest request) {
    return timeLogService.createFromWorklog(request);
  }

  @PostMapping("/importTimeLogs")
  public void importTimeLogs(@RequestBody final TimeLogImportRequest request) {
    timeLogService.importTimeLogs(request);
  }

  @PostMapping("/divide/{timeLogId}")
  public void divide(@PathVariable final long timeLogId) {
    timeLogService.divide(timeLogId);
  }

  @GetMapping("/{timeLogId}")
  public TimeLogGetResponse get(@PathVariable final long timeLogId) {
    return timeLogService.get(timeLogId);
  }

  @GetMapping("/hoursForWeek")
  public TimeLogWeekResponse getHoursForWeek(@RequestParam final LocalDate date, @RequestParam final Boolean includeTickets) {
    if (includeTickets) {
      return syncExternalServiceProcessingService.processWeekDayInfos(
          syncJiraProcessingService.processWeekDayInfos(
              timeLogService.getHoursForWeekWithTickets(date)
          )
      );
    } else {
      return syncExternalServiceProcessingService.processWeekDayInfos(timeLogService.getHoursForWeek(date));
    }
  }

  @GetMapping("/hoursForMonth")
  public TimeLogHoursForMonthResponse getHoursForMonth(@RequestParam final LocalDate date) {
    return syncExternalServiceProcessingService.processMonthDayInfos(
        syncJiraProcessingService.processMonthDayInfos(
            timeLogService.getHoursForMonth(date)
        )
    );
  }

  @PutMapping("/{timeLogId}")
  public TimeLogUpdateResponse update(@PathVariable final long timeLogId, @RequestBody final TimeLogUpdateRequest request) {
    return timeLogService.update(timeLogId, request);
  }

  @DeleteMapping("/{timeLogId}")
  public void delete(@PathVariable final long timeLogId) {
    timeLogService.delete(timeLogId);
  }

  @PatchMapping("/setGroupDescription")
  public void setGroupDescription(@RequestBody final TimeLogSetGroupDescrRequest request) {
    timeLogService.setGroupDescription(request);
  }

  @PatchMapping("/{timeLogId}/changeDate")
  public void changeDate(@PathVariable final long timeLogId, @RequestBody final TimeLogChangeDateRequest request) {
    timeLogService.changeDate(timeLogId, request);
  }

}
