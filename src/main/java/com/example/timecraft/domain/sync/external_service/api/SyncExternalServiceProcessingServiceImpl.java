package com.example.timecraft.domain.sync.external_service.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.domain.external_timelog.api.ExternalTimeLogSyncService;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogListResponse;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.example.timecraft.domain.sync.external_service.model.ExternalServiceSyncInfo;
import com.example.timecraft.domain.sync.external_service.util.SyncExternalServiceUtils;
import com.example.timecraft.domain.sync.jira.util.SyncUtils;
import com.example.timecraft.domain.sync.model.SyncStatus;
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
public class SyncExternalServiceProcessingServiceImpl implements SyncExternalServiceProcessingService {
  private final TimeLogSyncService timeLogSyncService;
  private final ExternalTimeLogSyncService externalTimeLogSyncService;
  private final AppProperties props;

  @Override
  public TimeLogListResponse processTimeLogDtos(final TimeLogListResponse response) {
    final int offset = props.getConfig().getOffset();
    final List<TimeLogListResponse.TimeLogDto> timeLogDtos = response.getItems();
    return new TimeLogListResponse(
        timeLogDtos.stream().peek(timeLogDto ->
            timeLogDto.setExternalServiceSyncInfo(
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
            .peek(dayInfo -> dayInfo.setExternalServiceSyncInfo(ExternalServiceSyncInfo.builder().status(determineSyncStatusForDay(dayInfo.getDate())).build()))
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
            .peek(dayInfo -> dayInfo.setExternalServiceSyncInfo(ExternalServiceSyncInfo.builder().status(determineSyncStatusForDay(dayInfo.getDate())).build()))
            .toList());
  }

  @Override
  public TimeLogHoursForMonthResponse processMonthDayInfos(final TimeLogHoursForMonthResponse response) {
    final List<TimeLogHoursForMonthResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForMonthResponse(response.getTotalHours(),
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setExternalServiceSyncInfo(ExternalServiceSyncInfo.builder().status(determineSyncStatusForDay(dayInfo.getDate())).build()))
            .toList());
  }

  @Override
  public ExternalTimeLogListResponse processExternalTimeLogDtos(final ExternalTimeLogListResponse response) {
    final List<ExternalTimeLogListResponse.ExternalTimeLogDto> externalTimeLogDtos = response.getItems();
    return new ExternalTimeLogListResponse(
        externalTimeLogDtos.stream().peek(externalTimeLogDto -> externalTimeLogDto.setExternalServiceSyncInfo(
                getExternalTimeLogSyncInfo(
                    externalTimeLogDto.getDate(),
                    externalTimeLogDto.getDescription())
            )
        ).toList()
    );
  }

  private ExternalServiceSyncInfo getExternalTimeLogSyncInfo(final LocalDate processedDate, final String description) {
    return ExternalServiceSyncInfo.builder()
        .status(getSyncStatus(processedDate, description))
        .color(SyncUtils.generateColor(description))
        .build();
  }

  private SyncStatus getSyncStatus(final LocalDate date, final String description) {
    final Boolean externalServiceIncludeDescription = props.getConfig().getExternalServiceIncludeDescription();

    final List<TimeLogEntity> timeLogEntityList;
    final List<ExternalTimeLogEntity> externalTimeLogEntityList;
    if (externalServiceIncludeDescription) {
      timeLogEntityList = timeLogSyncService.getAllByDateAndDescription(date, description);
      externalTimeLogEntityList = externalTimeLogSyncService.getAllByDateAndDescription(date, description);

    } else {
      timeLogEntityList = timeLogSyncService.getAllByDate(date);
      externalTimeLogEntityList = externalTimeLogSyncService.getAllByDate(date);

    }
    return calculateStatus(timeLogEntityList, externalTimeLogEntityList);
  }

  private SyncStatus calculateStatus(final List<TimeLogEntity> timeLogs, final List<ExternalTimeLogEntity> externalTimeLogs) {
    final boolean isCompatibleInTime = SyncExternalServiceUtils.isExternalTimeLogsAndTimeLogsCompatibleInTime(externalTimeLogs, timeLogs);
    if (isCompatibleInTime) {
      return SyncStatus.SYNCED;
    } else if (!timeLogs.isEmpty() && !externalTimeLogs.isEmpty()) {
      return SyncStatus.PARTIAL_SYNCED;
    } else {
      return SyncStatus.NOT_SYNCED;
    }
  }
}
