package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.timecraft.domain.sync.external_timelog.model.ExternalTimeLogSyncInfo;
import com.example.timecraft.domain.sync.jira.model.JiraSyncInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TimeLogHoursForMonthResponse {
  private String totalHours;
  private List<DayInfo> items;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DayInfo {
    private LocalDate date;
    private String duration;
    private JiraSyncInfo jiraSyncInfo;
    private ExternalTimeLogSyncInfo externalTimeLogSyncInfo;
    private boolean isConflicted;
  }
}
