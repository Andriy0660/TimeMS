package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime start;
    private String title;
    private boolean isSynchronized;
    private boolean isConflicted;
    private boolean isInProgress;
  }
}
