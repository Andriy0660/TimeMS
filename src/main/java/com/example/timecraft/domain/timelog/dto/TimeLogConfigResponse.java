package com.example.timecraft.domain.timelog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogConfigResponse {
  private Boolean isJiraSyncingEnabled;
  private Boolean isUpworkSyncingEnabled;
  private Double upworkTimeCf;
  private Integer offset;
  private Integer startHourOfWorkingDay;
  private Integer endHourOfWorkingDay;
}