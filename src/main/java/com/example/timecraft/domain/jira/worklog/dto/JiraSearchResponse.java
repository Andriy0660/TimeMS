package com.example.timecraft.domain.jira.worklog.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraSearchResponse {
  private List<IssueDto> issues;
  private Integer total;
}
