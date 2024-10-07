package com.example.timecraft.domain.sync.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncFromJiraRequest {
  private String ticket;
  private LocalDate date;
  private String description;
}
