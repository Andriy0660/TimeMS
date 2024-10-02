package com.example.timecraft.domain.worklog.dto;

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
  private double progress;
  private int currentIssueNumber;
  private int totalIssues;
  private String duration;
  private List<WorklogInfo> worklogInfos;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class WorklogInfo {
    private String ticket;
    private String comment;
  }

}
