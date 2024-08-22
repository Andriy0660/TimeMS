package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TimeLogHoursForWeekResponse {
  private List<DayInfo> items;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DayInfo {
    private String dayName;
    private LocalDate date;
    private List<TicketDuration> ticketDurations;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TicketDuration {
    private String ticket;
    private String duration;
  }
}
