package com.example.timecraft.domain.worklog.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorklogCreateFromTimeLogResponse {
  private Long id;
  private String author;
  private String ticket;
  private LocalDate date;
  private LocalTime startTime;
  private String comment;
  private Integer timeSpentSeconds;
}
