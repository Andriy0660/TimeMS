package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogImportRequest {
  private List<TimeLogDateGroup> dateGroups;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TimeLogDateGroup {
    private LocalDate key;
    private List<TimeLogDto> items;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TimeLogDto {
    private String ticket;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
  }
}
