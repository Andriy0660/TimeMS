package com.example.timecraft.domain.sync.jira.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.domain.sync.jira.model.JiraSyncInfo;
import com.example.timecraft.domain.sync.jira.util.SyncJiraUtils;
import com.example.timecraft.domain.sync.model.SyncStatus;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.api.TimeLogSyncService;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.api.WorklogSyncService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncJiraProcessingServiceImpl implements SyncJiraProcessingService {
  private final AppProperties props;
  private final TimeLogSyncService timeLogSyncService;
  private final WorklogSyncService worklogSyncService;

  public TimeLogListResponse processTimeLogDtos(final TimeLogListResponse response) {
    final int offset = props.getTimeConfig().getOffset();
    final List<TimeLogListResponse.TimeLogDto> timeLogDtos = response.getItems();
    return new TimeLogListResponse(
        timeLogDtos.stream().peek(timeLogDto ->
            timeLogDto.setJiraSyncInfo(JiraSyncInfo.builder()
                .status(getSyncStatus(
                    TimeLogUtils.getProcessedDate(timeLogDto.getDate(), timeLogDto.getStartTime(), offset),
                    timeLogDto.getTicket(),
                    timeLogDto.getDescription()))
                .color(TimeLogUtils.generateColor(
                    timeLogDto.getTicket(),
                    SyncJiraUtils.removeNonLetterAndDigitCharacters(timeLogDto.getDescription())
                ))
                .build())
        ).toList());
  }

  public TimeLogHoursForWeekResponse processWeekDayInfos(final TimeLogHoursForWeekResponse response) {
    final List<TimeLogHoursForWeekResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForWeekResponse(
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setSyncStatus(determineSyncStatusForDay(dayInfo.getDate())))
            .toList());
  }

  private SyncStatus determineSyncStatusForDay(final LocalDate date) {
    if (hasTimeLogsSyncStatusForDay(date, SyncStatus.NOT_SYNCED) ||
        hasWorklogsSyncStatusForDay(date, SyncStatus.NOT_SYNCED)) {
      return SyncStatus.NOT_SYNCED;
    } else if (hasTimeLogsSyncStatusForDay(date, SyncStatus.PARTIAL_SYNCED) ||
        hasWorklogsSyncStatusForDay(date, SyncStatus.PARTIAL_SYNCED)) {
      return SyncStatus.PARTIAL_SYNCED;
    } else {
      return SyncStatus.SYNCED;
    }
  }

  public TimeLogHoursForMonthResponse processMonthDayInfos(final TimeLogHoursForMonthResponse response) {
    final List<TimeLogHoursForMonthResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForMonthResponse(response.getTotalHours(),
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setSyncStatus(determineSyncStatusForDay(dayInfo.getDate())))
            .toList());
  }

  public WorklogListResponse processWorklogDtos(final WorklogListResponse response) {
    final int offset = props.getTimeConfig().getOffset();
    final List<WorklogListResponse.WorklogDto> worklogDtos = response.getItems();
    return new WorklogListResponse(
        worklogDtos.stream().peek(worklogDto -> worklogDto.setSyncStatus(
            getSyncStatus(
                TimeLogUtils.getProcessedDate(worklogDto.getDate(), worklogDto.getStartTime(), offset),
                worklogDto.getTicket(),
                worklogDto.getComment())
        )).toList()
    );
  }

  private SyncStatus getSyncStatus(final LocalDate date, final String ticket, final String description) {
    final List<TimeLogEntity> timeLogEntityList = timeLogSyncService.getAllByDateAndDescriptionAndTicket(date, description, ticket);
    final List<WorklogEntity> worklogEntityList = worklogSyncService.getAllByDateAndCommentAndTicket(date, description, ticket);

    final boolean isCompatibleInTime = SyncJiraUtils.isWorklogsAndTimeLogsCompatibleInTime(timeLogEntityList, worklogEntityList);
    if (isCompatibleInTime) {
      return SyncStatus.SYNCED;
    } else if (!timeLogEntityList.isEmpty() && !worklogEntityList.isEmpty()) {
      return SyncStatus.PARTIAL_SYNCED;
    } else {
      return SyncStatus.NOT_SYNCED;
    }
  }

  private boolean hasTimeLogsSyncStatusForDay(final LocalDate date, final SyncStatus syncStatus) {
    final int offset = props.getTimeConfig().getOffset();
    return timeLogSyncService.getAllByDate(date).stream().anyMatch(
        timeLogEntity -> getSyncStatus(
            TimeLogUtils.getProcessedDate(timeLogEntity.getDate(), timeLogEntity.getStartTime(), offset),
            timeLogEntity.getTicket(),
            timeLogEntity.getDescription()
        ).equals(syncStatus)
    );
  }

  private boolean hasWorklogsSyncStatusForDay(final LocalDate date, final SyncStatus syncStatus) {
    return worklogSyncService.getAllByDate(date).stream()
        .anyMatch(
            worklogEntity -> getSyncStatus(worklogEntity.getDate(), worklogEntity.getTicket(), worklogEntity.getComment()).equals(syncStatus)
        );
  }
}
