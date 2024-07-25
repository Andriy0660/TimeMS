package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeLogCreateResponse {
  private Long id;
  private String ticket;
  private LocalDateTime startTime;
  private String description;
  private boolean isConflicted;
}
