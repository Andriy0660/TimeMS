package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeLogCreateFormWorklogResponse {
  private Long id;
  private String ticket;
  private LocalDate date;
  private LocalTime startTime;
  private LocalTime endTime;
  private String description;
}
