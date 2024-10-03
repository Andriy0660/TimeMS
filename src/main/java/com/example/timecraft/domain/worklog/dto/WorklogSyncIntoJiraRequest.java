package com.example.timecraft.domain.worklog.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorklogSyncIntoJiraRequest {
  private String ticket;
  private LocalDate date;
  private String description;
  private int totalSpent;
}
