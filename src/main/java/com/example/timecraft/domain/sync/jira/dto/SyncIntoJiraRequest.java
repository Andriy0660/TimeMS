package com.example.timecraft.domain.sync.jira.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncIntoJiraRequest {
  private String ticket;
  private LocalDate date;
  private String description;
  private int totalSpent;
}
