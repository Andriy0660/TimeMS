package com.example.timecraft.domain.sync.jira;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.jira_instance.aop.RequireJiraAccount;
import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncJiraProgressResponse;
import com.example.timecraft.domain.sync.jira.service.SyncJiraService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/syncJira")
@RequireJiraAccount
public class SyncJiraController {
  private final SyncJiraService syncJiraService;

  @PostMapping("/from")
  public void syncFromJira(@RequestBody final SyncFromJiraRequest request) {
    syncJiraService.syncFromJira(request);
  }

  @PostMapping("/to")
  public void syncIntoJira(@RequestBody final SyncIntoJiraRequest request) {
    syncJiraService.syncIntoJira(request);
  }

  @PostMapping("/syncAllWorklogs")
  public void syncWorklogs() {
    syncJiraService.syncAllWorklogs();
  }

  @PostMapping("/{ticket}")
  public void syncWorklogsForIssue(@PathVariable final String ticket) {
    syncJiraService.syncWorklogsForTicket(ticket);
  }

  @GetMapping("/progress")
  public SyncJiraProgressResponse getProgress() {
    return syncJiraService.getProgress();
  }
}
