package com.example.timecraft.domain.worklog.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.worklog.dto.WorklogProgressResponse;

@Service
public class SyncProgressService {
  private final AtomicReference<Double> progress = new AtomicReference<>(0.0);
  private final AtomicReference<List<WorklogProgressResponse.WorklogInfo>> worklogInfos = new AtomicReference<>(null);
  private final AtomicReference<Integer> currentIssueNumber = new AtomicReference<>(0);
  private final AtomicReference<Integer> totalIssues = new AtomicReference<>(0);
  private final AtomicReference<LocalDateTime> startTime = new AtomicReference<>(null);

  public double getProgress() {
    return progress.get();
  }

  public void setProgress(double value) {
    progress.set(value);
  }

  public void clearProgress() {
    progress.set(0.0);
    currentIssueNumber.set(0);
    totalIssues.set(0);
    startTime.set(null);
  }

  public List<WorklogProgressResponse.WorklogInfo> getWorklogInfos() {
    return worklogInfos.get();
  }

  public void setWorklogInfos(final List<WorklogProgressResponse.WorklogInfo> worklogInfos) {
    this.worklogInfos.set(worklogInfos);
  }

  public int getCurrentIssueNumber() {
    return currentIssueNumber.get();
  }

  public void setCurrentIssueNumber(final int currentIssueNumber) {
    this.currentIssueNumber.set(currentIssueNumber);
  }

  public int getTotalIssues() {
    return totalIssues.get();
  }

  public void setTotalIssues(final int totalIssues) {
    this.totalIssues.set(totalIssues);
  }

  public LocalDateTime getStartTime() {
    return startTime.get();
  }

  public void setStartTime(final LocalDateTime startTime) {
    this.startTime.set(startTime);
  }
}