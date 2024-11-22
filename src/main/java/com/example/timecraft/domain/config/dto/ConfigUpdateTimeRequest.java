package com.example.timecraft.domain.config.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdateTimeRequest {
  private Integer dayOffsetHour;
  private Integer workingDayStartHour;
  private Integer workingDayEndHour;
}
