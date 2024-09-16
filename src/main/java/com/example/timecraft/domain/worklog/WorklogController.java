package com.example.timecraft.domain.worklog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.worklog.dto.WorklogProgressResponse;
import com.example.timecraft.domain.worklog.service.WorklogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/work-logs")
public class WorklogController {
  private final WorklogService worklogService;

  @PostMapping
  public void synchronizeWorklogs() {
    worklogService.synchronizeWorklogs();
  }

  @PostMapping("/{issueKey}")
  public void synchronizeWorklogsForIssue(@PathVariable String issueKey) {
    worklogService.synchronizeWorklogsForIssue(issueKey);
  }

  @GetMapping
  public WorklogProgressResponse getProgress() {
    return worklogService.getProgress();
  }
}
