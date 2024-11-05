package com.example.timecraft.domain.sync.external_timelog.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.domain.external_timelog.api.ExternalTimeLogSyncService;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogListResponse;
import com.example.timecraft.domain.sync.external_timelog.model.ExternalTimeLogSyncInfo;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.example.timecraft.domain.sync.external_timelog.util.SyncExternalTimeLogUtils;
import com.example.timecraft.domain.sync.model.SyncStatus;
import com.example.timecraft.domain.sync.util.SyncUtils;
import com.example.timecraft.domain.timelog.api.TimeLogSyncService;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekWithTicketsResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogWeekResponse;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncExternalTimeLogProcessingServiceImpl implements SyncExternalTimeLogProcessingService {
  private final TimeLogSyncService timeLogSyncService;
  private final ExternalTimeLogSyncService externalTimeLogSyncService;
  private final AppProperties props;

  @Override
  public TimeLogListResponse processTimeLogDtos(final TimeLogListResponse response) {
    final int offset = props.getConfig().getOffset();
    final List<TimeLogListResponse.TimeLogDto> timeLogDtos = response.getItems();
    return new TimeLogListResponse(
        timeLogDtos.stream().peek(timeLogDto ->
            timeLogDto.setExternalTimeLogSyncInfo(
                getExternalTimeLogSyncInfo(
                    TimeLogUtils.getProcessedDate(timeLogDto.getDate(), timeLogDto.getStartTime(), offset),
                    timeLogDto.getDescription()))
        ).toList()
    );
  }

  @Override
  public TimeLogWeekResponse processWeekDayInfos(final TimeLogHoursForWeekWithTicketsResponse response) {
    final List<TimeLogHoursForWeekWithTicketsResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForWeekWithTicketsResponse(
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setExternalTimeLogSyncInfo(ExternalTimeLogSyncInfo.builder().status(determineSyncStatusForDay(dayInfo.getDate())).build()))
            .toList());
  }

  private SyncStatus determineSyncStatusForDay(final LocalDate date) {
    if (hasTimeLogsSyncStatusForDay(date, SyncStatus.NOT_SYNCED) ||
        hasExternalTimeLogsSyncStatusForDay(date, SyncStatus.NOT_SYNCED)) {
      return SyncStatus.NOT_SYNCED;
    } else if (hasTimeLogsSyncStatusForDay(date, SyncStatus.PARTIAL_SYNCED) ||
        hasExternalTimeLogsSyncStatusForDay(date, SyncStatus.PARTIAL_SYNCED)) {
      return SyncStatus.PARTIAL_SYNCED;
    } else {
      return SyncStatus.SYNCED;
    }
  }

  private boolean hasTimeLogsSyncStatusForDay(final LocalDate date, final SyncStatus syncStatus) {
    final int offset = props.getConfig().getOffset();
    return timeLogSyncService.getAllByDate(date).stream().anyMatch(
        timeLogEntity -> getSyncStatus(
            TimeLogUtils.getProcessedDate(timeLogEntity.getDate(), timeLogEntity.getStartTime(), offset),
            timeLogEntity.getDescription()
        ).equals(syncStatus)
    );
  }

  private boolean hasExternalTimeLogsSyncStatusForDay(final LocalDate date, final SyncStatus syncStatus) {
    return externalTimeLogSyncService.getAllByDate(date).stream()
        .anyMatch(
            externalTimeLogEntity -> getSyncStatus(externalTimeLogEntity.getDate(), externalTimeLogEntity.getDescription()).equals(syncStatus)
        );
  }

  @Override
  public TimeLogWeekResponse processWeekDayInfos(final TimeLogHoursForWeekResponse response) {
    final List<TimeLogHoursForWeekResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForWeekResponse(
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setExternalTimeLogSyncInfo(ExternalTimeLogSyncInfo.builder().status(determineSyncStatusForDay(dayInfo.getDate())).build()))
            .toList());
  }

  @Override
  public TimeLogHoursForMonthResponse processMonthDayInfos(final TimeLogHoursForMonthResponse response) {
    final List<TimeLogHoursForMonthResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForMonthResponse(response.getTotalHours(),
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setExternalTimeLogSyncInfo(ExternalTimeLogSyncInfo.builder().status(determineSyncStatusForDay(dayInfo.getDate())).build()))
            .toList());
  }

  @Override
  public ExternalTimeLogListResponse processExternalTimeLogDtos(final ExternalTimeLogListResponse response) {
    final int offset = props.getConfig().getOffset();
    final List<ExternalTimeLogListResponse.ExternalTimeLogDto> externalTimeLogDtos = response.getItems();
    return new ExternalTimeLogListResponse(
        externalTimeLogDtos.stream().peek(externalTimeLogDto -> externalTimeLogDto.setExternalTimeLogSyncInfo(
            getExternalTimeLogSyncInfo(
                TimeLogUtils.getProcessedDate(externalTimeLogDto.getDate(), externalTimeLogDto.getStartTime(), offset),
                externalTimeLogDto.getDescription())
            )
        ).toList()
    );
  }

  private ExternalTimeLogSyncInfo getExternalTimeLogSyncInfo(final LocalDate processedDate, final String description) {
    return ExternalTimeLogSyncInfo.builder()
        .status(getSyncStatus(processedDate, description))
        .color(SyncUtils.generateColor(description))
        .build();
  }

  private SyncStatus getSyncStatus(final LocalDate date, final String description) {
    final Boolean externalServiceIncludeDescription = props.getConfig().getExternalServiceIncludeDescription();

    if (externalServiceIncludeDescription) {
      final List<TimeLogEntity> timeLogEntityList = timeLogSyncService.getAllByDateAndDescription(date, description);
      final List<ExternalTimeLogEntity> externalTimeLogEntityList = externalTimeLogSyncService.getAllByDateAndDescription(date, description);

      return calculateStatus(timeLogEntityList, externalTimeLogEntityList);

    } else {
      final List<TimeLogEntity> timeLogEntityList = timeLogSyncService.getAllByDate(date);
      final List<ExternalTimeLogEntity> externalTimeLogEntityList = externalTimeLogSyncService.getAllByDate(date);

      return calculateStatus(timeLogEntityList, externalTimeLogEntityList);
    }
  }

  private SyncStatus calculateStatus(final List<TimeLogEntity> timeLogEntityList, final List<ExternalTimeLogEntity> externalTimeLogEntityList) {
    final int timeLogsSpentSeconds = TimeLogUtils.getTotalSpentSecondsForTimeLogs(timeLogEntityList);
    final int externalTimeLogsSpentSeconds = SyncExternalTimeLogUtils.getTotalSpentSecondsForExternalTimeLogs(externalTimeLogEntityList);

    final boolean isCompatibleInTime = timeLogsSpentSeconds == externalTimeLogsSpentSeconds;
    if (isCompatibleInTime) {
      return SyncStatus.SYNCED;
    } else if (!timeLogEntityList.isEmpty() && !externalTimeLogEntityList.isEmpty()) {
      return SyncStatus.PARTIAL_SYNCED;
    } else {
      return SyncStatus.NOT_SYNCED;
    }
  }



}
