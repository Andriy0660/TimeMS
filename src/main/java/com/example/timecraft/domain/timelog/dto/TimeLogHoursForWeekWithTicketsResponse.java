package com.example.timecraft.domain.timelog.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.timecraft.domain.external_timelog.model.ExternalTimeLogSyncInfo;
import com.example.timecraft.domain.sync.jira.model.JiraSyncInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TimeLogHoursForWeekWithTicketsResponse implements TimeLogWeekResponse {
  private List<DayInfo> items;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DayInfo {
    private String dayName;
    private LocalDate date;
    private JiraSyncInfo jiraSyncInfo;
    private ExternalTimeLogSyncInfo externalTimeLogSyncInfo;
    private boolean isConflicted;
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
