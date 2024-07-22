package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TimeLogListAllResponse {
  private List<LogEntryDto> items;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class LogEntryDto {
    private Long id;
    private String ticket;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private String totalTime;
  }
}
