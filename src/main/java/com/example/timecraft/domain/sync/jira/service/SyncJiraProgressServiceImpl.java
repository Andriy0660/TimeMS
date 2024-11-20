package com.example.timecraft.domain.sync.jira.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.timecraft.domain.sync.jira.model.SyncUserJiraProgress;

@Service
public class SyncJiraProgressServiceImpl implements SyncJiraProgressService {
  private final Map<Long, SyncUserJiraProgress> userProgresses = new ConcurrentHashMap<>();

  public SyncUserJiraProgress createUserProgress(final Long userId) {
    final SyncUserJiraProgress syncUserJiraProgress = new SyncUserJiraProgress();
    userProgresses.put(userId, syncUserJiraProgress);
    return syncUserJiraProgress;
  }

  public SyncUserJiraProgress getUserProgress(final Long userId) {
    return userProgresses.get(userId);
  }

  public void removeUserProgress(final Long userId) {
    userProgresses.remove(userId);
  }

  @Scheduled(fixedRate = 600000) //10min
  public void cleanOldProgresses() {
    final LocalDateTime timeToRemove = LocalDateTime.now().minusMinutes(30);
    userProgresses.entrySet().removeIf(entry ->
        entry.getValue().getEndTime() != null &&
            entry.getValue().getEndTime().isBefore(timeToRemove));
  }
}
