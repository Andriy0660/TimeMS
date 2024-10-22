package com.example.timecraft.domain.jira.worklog.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JiraWorklogCreateDto {
  private String started;
  private Map<String, ?> comment;
  private Integer timeSpentSeconds;
}
