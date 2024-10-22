package com.example.timecraft.domain.sync.jira.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.example.timecraft.domain.sync.jira.dto.SyncJiraProgressResponse;

@Component
public class SyncJiraProgress {
  private final AtomicReference<Boolean> isInProgress = new AtomicReference<>(false);
  private final AtomicReference<Double> progress = new AtomicReference<>(0.0);
  private final AtomicReference<List<SyncJiraProgressResponse.WorklogInfo>> worklogInfos = new AtomicReference<>(null);
  private final AtomicReference<LocalDateTime> startTime = new AtomicReference<>(null);
  private final AtomicReference<LocalDateTime> endTime = new AtomicReference<>(null);
  private final AtomicReference<Integer> currentIssueNumber = new AtomicReference<>(0);
  private final AtomicReference<Integer> totalIssues = new AtomicReference<>(0);
  private final AtomicReference<Integer> totalTimeSpent = new AtomicReference<>(0);
  private final AtomicReference<Integer> totalEstimate = new AtomicReference<>(0);

  public void clearProgress() {
    isInProgress.set(false);
    progress.set(0.0);
    currentIssueNumber.set(0);
    totalIssues.set(0);
    startTime.set(null);
    endTime.set(null);
    totalTimeSpent.set(0);
    totalEstimate.set(0);
    worklogInfos.set(null);
  }

  public void setIsInProgress(final boolean isInProgress) {
    this.isInProgress.set(isInProgress);
  }

  public boolean isInProgress() {
    return isInProgress.get();
  }

  public double getProgress() {
    return progress.get();
  }

  public void setProgress(double value) {
    progress.set(value);
  }

  public List<SyncJiraProgressResponse.WorklogInfo> getWorklogInfos() {
    return worklogInfos.get();
  }

  public void setWorklogInfos(final List<SyncJiraProgressResponse.WorklogInfo> worklogInfos) {
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

  public LocalDateTime getEndTime() {
    return endTime.get();
  }

  public void setEndTime(final LocalDateTime endTime) {
    this.endTime.set(endTime);
  }

  public int getTotalTimeSpent() {
    return totalTimeSpent.get();
  }

  public void setTotalTimeSpent(final int totalTimeSpent) {
    this.totalTimeSpent.set(totalTimeSpent);
  }

  public int getTotalEstimate() {
    return totalEstimate.get();
  }

  public void setTotalEstimate(final int timeOriginalEstimate) {
    this.totalEstimate.set(timeOriginalEstimate);
  }
}