package com.example.timecraft.domain.sync.external_service.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncIntoExternalServiceRequest {
  private LocalDate date;
  private String description;
}
