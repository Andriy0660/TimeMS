package com.example.timecraft.domain.logEntry.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEntryGetResponse {
  private Long id;
  private String ticket;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String description;
  private String totalTime;
}
