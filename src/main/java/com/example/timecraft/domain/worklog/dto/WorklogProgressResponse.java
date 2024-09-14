package com.example.timecraft.domain.worklog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorklogProgressResponse {
  private double progress;
  private String ticketOfCurrentWorklog;
  private String commentOfCurrentWorklog;
}
