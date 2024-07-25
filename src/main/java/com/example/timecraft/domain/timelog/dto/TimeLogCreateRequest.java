package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogCreateRequest {
  private String ticket;
  private LocalDateTime startTime;
  private String description;
}
