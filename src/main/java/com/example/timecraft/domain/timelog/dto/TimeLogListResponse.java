package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private String totalTime;
  }
}

