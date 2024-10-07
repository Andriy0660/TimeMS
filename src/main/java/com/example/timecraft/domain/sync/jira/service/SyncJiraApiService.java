package com.example.timecraft.domain.sync.jira.service;

import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;

public interface SyncJiraApiService {
  void syncFromJira(SyncFromJiraRequest request);

  void syncIntoJira(SyncIntoJiraRequest request);

}
