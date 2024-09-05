package com.example.timecraft.domain.timelog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogConfigResponse {
  private Integer offset;
  private Integer startHourOfWorkingDay;
  private Integer endHourOfWorkingDay;
}