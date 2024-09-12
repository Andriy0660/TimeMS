package com.example.timecraft.domain.worklog.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorklogJiraDto {
  private Long id;
  private String author;
  private String ticket;
  private LocalDate date;
  private LocalTime startTime;
  private String comment;
  private Integer timeSpentSeconds;
}
