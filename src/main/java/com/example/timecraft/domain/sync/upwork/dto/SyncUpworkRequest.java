package com.example.timecraft.domain.sync.upwork.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncUpworkRequest {
  private LocalDate date;
  private Integer timeSpentSeconds;
}
