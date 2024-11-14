package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.example.timecraft.domain.sync.external_service.model.ExternalServiceSyncInfo;
import com.example.timecraft.domain.sync.jira.model.JiraSyncInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TimeLogListResponse {
  private List<TimeLogDto> items;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TimeLogDto {
    private Long id;
    private String ticket;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private String totalTime;
    private JiraSyncInfo jiraSyncInfo;
    private ExternalServiceSyncInfo externalServiceSyncInfo;
    private List<String> labels;
  }
}

