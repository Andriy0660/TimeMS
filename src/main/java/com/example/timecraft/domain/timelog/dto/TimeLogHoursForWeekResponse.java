package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.timecraft.domain.sync.upwork.model.UpworkSyncInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TimeLogHoursForWeekResponse implements TimeLogWeekResponse {
  private List<DayInfo> items;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DayInfo {
    private String dayName;
    private LocalDate date;
    private String duration;
    private UpworkSyncInfo upworkSyncInfo;
    private boolean isConflicted;
  }
}