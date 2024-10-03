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

import com.example.timecraft.domain.logsync.service.LogSyncService;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.dto.WorklogProgressResponse;
import com.example.timecraft.domain.worklog.dto.WorklogSyncIntoJiraRequest;
import com.example.timecraft.domain.worklog.service.WorklogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/work-logs")
public class WorklogController {
  private final WorklogService worklogService;
  private final LogSyncService logSyncService;

  @GetMapping()
  public WorklogListResponse listWorklogs(@RequestParam final String mode, @RequestParam final LocalDate date) {
    return logSyncService.processWorklogDtos(worklogService.list(mode, date));
  }

  @PostMapping
  public WorklogCreateFromTimeLogResponse createWorklogFromTimeLog(@RequestBody final WorklogCreateFromTimeLogRequest request) {
    return worklogService.createWorklogFromTimeLog(request);
  }

  @PostMapping("/syncIntoJira")
  public void syncIntoJira (@RequestBody final WorklogSyncIntoJiraRequest request) {
    worklogService.syncIntoJira(request);
  }

  @DeleteMapping("/{issueKey}/{worklogId}")
  public void deleteUnsyncedWorklog(@PathVariable String issueKey, @PathVariable Long worklogId) {
    worklogService.deleteUnsyncedWorklog(issueKey, worklogId);
  }

  @PostMapping("/synchronizeWorklogs")
  public void syncWorklogs() {
    worklogService.syncWorklogs();
  }

  @PostMapping("/{issueKey}")
  public void syncWorklogsForIssue(@PathVariable String issueKey) {
    worklogService.syncWorklogsForIssue(issueKey);
  }

  @GetMapping("/progress")
  public WorklogProgressResponse getProgress() {
    return worklogService.getProgress();
  }
}
