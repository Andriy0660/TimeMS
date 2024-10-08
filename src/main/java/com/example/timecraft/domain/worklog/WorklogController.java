package com.example.timecraft.domain.worklog;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.sync.jira.service.SyncJiraProcessingService;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.service.WorklogApiService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/work-logs")
public class WorklogController {
  private final WorklogApiService worklogApiService;
  private final SyncJiraProcessingService syncJiraProcessingService;

  @GetMapping()
  public WorklogListResponse listWorklogs(@RequestParam final LocalDate date) {
    return syncJiraProcessingService.processWorklogDtos(worklogApiService.list(date));
  }

  @PostMapping
  public WorklogCreateFromTimeLogResponse createFromTimeLog(@RequestBody final WorklogCreateFromTimeLogRequest request) {
    return worklogApiService.createFromTimeLog(request);
  }

  @DeleteMapping("/{issueKey}/{worklogId}")
  public void delete(@PathVariable final String issueKey, @PathVariable final Long worklogId) {
    worklogApiService.delete(issueKey, worklogId);
  }

}
