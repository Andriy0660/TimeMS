package com.example.timecraft.domain.worklog;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.timelog.service.TimeLogService;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.dto.WorklogProgressResponse;
import com.example.timecraft.domain.worklog.service.WorklogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/work-logs")
public class WorklogController {
  private final WorklogService worklogService;
  private final TimeLogService timeLogService;

  @GetMapping()
  public WorklogListResponse listNotSyncedWorklogs(@RequestParam final String mode, @RequestParam final LocalDate date) {
    return timeLogService.listNotSyncedWorklogs(mode, date);
  }

  @DeleteMapping("/{issueKey}/{worklogId}")
  public void deleteUnsyncedWorklog(@PathVariable String issueKey, @PathVariable Long worklogId) {
    worklogService.deleteUnsyncedWorklog(issueKey, worklogId);
  }

  @PostMapping
  public void synchronizeWorklogs() {
    worklogService.synchronizeWorklogs();
  }

  @PostMapping("/{issueKey}")
  public void synchronizeWorklogsForIssue(@PathVariable String issueKey) {
    worklogService.synchronizeWorklogsForIssue(issueKey);
  }

  @GetMapping("/progress")
  public WorklogProgressResponse getProgress() {
    return worklogService.getProgress();
  }
}
