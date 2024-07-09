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
public class LogEntryCreateResponse {
  private Long id;
  private String ticket;
  private LocalDateTime startTime;
  private String description;
}
