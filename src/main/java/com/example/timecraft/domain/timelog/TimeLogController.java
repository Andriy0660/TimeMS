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

import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.service.TimeLogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/time-logs")
public class TimeLogController {
  private final TimeLogService timeLogService;

  @GetMapping
  public TimeLogListResponse list(@RequestParam final String mode, final @RequestParam LocalDate date) {
    return timeLogService.list(mode, date);
  }

  @PostMapping
  public TimeLogCreateResponse create(@RequestBody final TimeLogCreateRequest request) {
    return timeLogService.create(request);
  }

  @GetMapping("/{logEntryId}")
  public TimeLogGetResponse get(@PathVariable final long logEntryId) {
    return timeLogService.get(logEntryId);
  }

  @PutMapping("/{logEntryId}")
  public TimeLogUpdateResponse update(@PathVariable final long logEntryId, @RequestBody final TimeLogUpdateRequest request) {
    return timeLogService.update(logEntryId, request);
  }

  @DeleteMapping("/{logEntryId}")
  public void delete(@PathVariable final long logEntryId) {
    timeLogService.delete(logEntryId);
  }

  @PatchMapping("/setGroupDescription")
  public void setGroupDescription(@RequestBody final TimeLogSetGroupDescrRequest request) {
    timeLogService.setGroupDescription(request);
  }

}
