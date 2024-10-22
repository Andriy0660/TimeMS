package com.example.timecraft.domain.jira.worklog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueDto {
  private String key;
  private Fields fields;

  @Getter
  @Setter
  public static class Fields {
    private int timespent;
    private int timeoriginalestimate;
  }
}
