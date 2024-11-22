package com.example.timecraft.domain.config.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigGetResponse {
  private Integer dayOffsetHour;
  private Integer workingDayStartHour;
  private Integer workingDayEndHour;
  private Boolean isJiraEnabled;
  private Boolean isExternalServiceEnabled;
  private Integer externalServiceTimeCf;
  private Boolean isExternalServiceIncludeDescription;
}
