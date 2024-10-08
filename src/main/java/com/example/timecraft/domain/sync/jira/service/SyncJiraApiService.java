package com.example.timecraft.domain.sync.jira.service;

import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncJiraProgressResponse;

public interface SyncJiraApiService {
  void syncFromJira(SyncFromJiraRequest request);

  void syncIntoJira(SyncIntoJiraRequest request);

  SyncJiraProgressResponse getProgress();

  void syncAllWorklogs();

  void syncWorklogsForTicket(final String ticket);
}
