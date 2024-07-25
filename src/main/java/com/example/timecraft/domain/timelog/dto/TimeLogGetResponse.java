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
public class TimeLogGetResponse {
  private Long id;
  private String ticket;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String description;
  private String totalTime;
}
