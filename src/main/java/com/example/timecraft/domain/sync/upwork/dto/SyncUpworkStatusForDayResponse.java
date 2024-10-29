package com.example.timecraft.domain.sync.upwork.dto;

import java.time.LocalDate;

import com.example.timecraft.domain.sync.upwork.model.UpworkSyncInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncUpworkStatusForDayResponse {
  private LocalDate date;
  private UpworkSyncInfo upworkSyncInfo;
}
