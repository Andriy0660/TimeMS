package com.example.timecraft.domain.worklog.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorklogProgressResponse {
  private boolean isInProgress;
  private double progress;
  private List<WorklogInfo> worklogInfos;
  private String duration;
  private LocalDateTime lastSyncedAt;
  private String totalTimeSpent;
  private String totalEstimate;
  private int currentIssueNumber;
  private int totalIssues;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class WorklogInfo {
    private LocalDate date;
    private String ticket;
    private String comment;
  }

}
