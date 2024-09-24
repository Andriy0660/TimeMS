package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogCreateFromWorklogRequest {
  private String ticket;
  private LocalDate date;
  private LocalTime startTime;
  private String description;
  private Integer timeSpentSeconds;

}
