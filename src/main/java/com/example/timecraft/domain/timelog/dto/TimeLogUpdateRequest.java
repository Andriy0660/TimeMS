package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogUpdateRequest {
  private LocalDate date;
  private String ticket;
  private LocalTime startTime;
  private LocalTime endTime;
  private String description;
}
