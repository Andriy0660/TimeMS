package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogUpdateRequest {
  private String ticket;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String description;
}
