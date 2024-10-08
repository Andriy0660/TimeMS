package com.example.timecraft.domain.sync.jira.service;

import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncJiraProgressResponse;

public interface SyncJiraApiService {
  void syncFromJira(final SyncFromJiraRequest request);

  void syncIntoJira(final SyncIntoJiraRequest request);

  SyncJiraProgressResponse getProgress();

  void syncAllWorklogs();

  void syncWorklogsForTicket(final String ticket);
}
