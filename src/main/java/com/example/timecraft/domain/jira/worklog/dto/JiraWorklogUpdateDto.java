package com.example.timecraft.domain.jira.worklog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JiraWorklogUpdateDto {
  private String started;
  private Integer timeSpentSeconds;
}
