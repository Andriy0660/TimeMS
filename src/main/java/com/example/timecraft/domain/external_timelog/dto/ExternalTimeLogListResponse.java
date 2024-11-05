package com.example.timecraft.domain.external_timelog.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.example.timecraft.domain.external_timelog.model.ExternalTimeLogSyncInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ExternalTimeLogListResponse {
  private List<ExternalTimeLogDto> items;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ExternalTimeLogDto {
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private ExternalTimeLogSyncInfo externalTimeLogSyncInfo;
  }
}
