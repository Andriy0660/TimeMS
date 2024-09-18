package com.example.timecraft.domain.jira.worklog.dto;

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
public class JiraWorklogDto {
  private Long id;
  private String author;
  private String issueKey;
  private LocalDate date;
  private LocalTime startTime;
  private String comment;
  private Integer timeSpentSeconds;
}
