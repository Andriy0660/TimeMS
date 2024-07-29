package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TimeLogListByDateAndDescriptionResponse {
  private Map<LocalDate, Map<String, List<TimeLogDto>>> items;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DescriptionInfo {
    private String description;
    private String totalTime;
  }

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
  }
}


