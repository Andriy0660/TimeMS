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

import com.example.timecraft.domain.jira_instance.aop.RequireJiraAccount;
import com.example.timecraft.domain.sync.jira.api.SyncJiraProcessingService;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.service.WorklogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/work-logs")
@RequireJiraAccount
public class WorklogController {
  private final WorklogService worklogService;
  private final SyncJiraProcessingService syncJiraProcessingService;

  @GetMapping()
  public WorklogListResponse list(@RequestParam final LocalDate date) {
    return syncJiraProcessingService.processWorklogDtos(worklogService.list(date));
  }

  @PostMapping
  public WorklogCreateFromTimeLogResponse createFromTimeLog(@RequestBody final WorklogCreateFromTimeLogRequest request) {
    return worklogService.createFromTimeLog(request);
  }

  @DeleteMapping("/{issueKey}/{id}")
  public void delete(@PathVariable final String issueKey, @PathVariable final Long id) {
    worklogService.delete(issueKey, id);
  }

}
