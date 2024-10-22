package com.example.timecraft.domain.worklog.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.example.timecraft.domain.sync.jira.model.JiraSyncInfo;
import com.example.timecraft.domain.sync.model.SyncStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class WorklogListResponse {
  private List<WorklogDto> items;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class WorklogDto {
    private Long id;
    private String author;
    private String ticket;
    private LocalDate date;
    private LocalTime startTime;
    private String comment;
    private Integer timeSpentSeconds;
    private SyncStatus syncStatus;
    private String color;
    private JiraSyncInfo jiraSyncInfo;
  }
}

