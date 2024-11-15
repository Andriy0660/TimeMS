package com.example.timecraft.domain.sync.jira.service;

import com.example.timecraft.domain.sync.jira.model.SyncUserJiraProgress;

public interface SyncJiraProgressService {
  SyncUserJiraProgress createUserProgress(final Long userId);

  SyncUserJiraProgress getUserProgress(final Long userId);

  void removeUserProgress(final Long userId);
}
