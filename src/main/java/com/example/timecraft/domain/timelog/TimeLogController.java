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

  @PostMapping("/from-worklog")
  public TimeLogCreateFormWorklogResponse createFromWorklog(@RequestBody final TimeLogCreateFromWorklogRequest request) {
    return timeLogService.createFromWorklog(request);
  }

  @PostMapping("/import-time-logs")
  public void importTimeLogs(@RequestBody final TimeLogImportRequest request) {
    timeLogService.importTimeLogs(request);
  }

  @PostMapping("/divide/{id}")
  public void divide(@PathVariable final long id) {
    timeLogService.divide(id);
  }

  @GetMapping("/{id}")
  public TimeLogGetResponse get(@PathVariable final long id) {
    return timeLogService.get(id);
  }

  @GetMapping("/week/hours")
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

  @GetMapping("/month/hours")
  public TimeLogHoursForMonthResponse getHoursForMonth(@RequestParam final LocalDate date) {
    return syncExternalServiceProcessingService.processMonthDayInfos(
        syncJiraProcessingService.processMonthDayInfos(
            timeLogService.getHoursForMonth(date)
        )
    );
  }

  @PutMapping("/{id}")
  public TimeLogUpdateResponse update(@PathVariable final long id, @RequestBody final TimeLogUpdateRequest request) {
    return timeLogService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable final long id) {
    timeLogService.delete(id);
  }

  @PatchMapping("/set-group-description")
  public void setGroupDescription(@RequestBody final TimeLogSetGroupDescrRequest request) {
    timeLogService.setGroupDescription(request);
  }

  @PatchMapping("/{id}/change-date")
  public void changeDate(@PathVariable final long id, @RequestBody final TimeLogChangeDateRequest request) {
    timeLogService.changeDate(id, request);
  }

}
