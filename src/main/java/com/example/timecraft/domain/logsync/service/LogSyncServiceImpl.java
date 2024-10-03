package com.example.timecraft.domain.logsync.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.domain.logsync.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.logsync.model.Status;
import com.example.timecraft.domain.logsync.util.LogSyncUtil;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.service.TimeLogService;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.service.WorklogService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogSyncServiceImpl implements LogSyncService {
  private final TimeLogService timeLogService;
  private final WorklogService worklogService;
  private final AppProperties props;

  @Override
  public void syncFromJira(final SyncFromJiraRequest request) {
    List<TimeLogEntity> timeLogEntityList = getTimeLogsForDay(request.getDate());
    timeLogEntityList = timeLogEntityList
        .stream()
        .filter(entity -> LogSyncUtil.areDescriptionsEqual(entity.getDescription(), request.getDescription()))
        .filter(entity -> Objects.equals(entity.getTicket(), request.getTicket()))
        .toList();
    timeLogService.delete(timeLogEntityList);

    List<WorklogEntity> worklogEntityList = getWorklogsForDay(request.getDate());
    worklogEntityList = worklogEntityList
        .stream()
        .filter(entity -> LogSyncUtil.areDescriptionsEqual(entity.getComment(), request.getDescription()))
        .filter(entity -> Objects.equals(entity.getTicket(), request.getTicket()))
        .toList();

    timeLogService.saveAll(worklogEntityList.stream().map(worklogEntity -> TimeLogEntity.builder()
            .description(worklogEntity.getComment())
            .date(worklogEntity.getDate())
            .ticket(worklogEntity.getTicket())
            .startTime(worklogEntity.getStartTime())
            .endTime(worklogEntity.getStartTime().plusSeconds(worklogEntity.getTimeSpentSeconds()))
            .build())
        .toList());

  }

  public TimeLogListResponse processTimeLogDtos(TimeLogListResponse response) {
    final int offset = props.getTimeConfig().getOffset();
    List<TimeLogListResponse.TimeLogDto> timeLogDtos = response.getItems();
    return new TimeLogListResponse(
        timeLogDtos.stream().peek(timeLogDto ->
            timeLogDto.setSyncStatus(
                getSyncStatus(
                    TimeLogUtils.getProcessedDate(timeLogDto.getDate(), timeLogDto.getStartTime(), offset),
                    timeLogDto.getTicket(),
                    timeLogDto.getDescription())
            )
        ).toList());
  }

  public TimeLogHoursForWeekResponse processWeekDayInfos(TimeLogHoursForWeekResponse response) {
    List<TimeLogHoursForWeekResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForWeekResponse(
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setSyncStatus(determineSyncStatusForDay(dayInfo.getDate())))
            .toList());
  }

  private Status determineSyncStatusForDay(LocalDate date) {
    if (hasTimeLogsSyncStatusForDay(date, Status.NOT_SYNCED) ||
        hasWorklogsSyncStatusForDay(date, Status.NOT_SYNCED)) {
      return Status.NOT_SYNCED;
    } else if (hasTimeLogsSyncStatusForDay(date, Status.PARTIAL_SYNCED) ||
        hasWorklogsSyncStatusForDay(date, Status.PARTIAL_SYNCED)) {
      return Status.PARTIAL_SYNCED;
    } else {
      return Status.SYNCED;
    }
  }

  public TimeLogHoursForMonthResponse processMonthDayInfos(TimeLogHoursForMonthResponse response) {
    List<TimeLogHoursForMonthResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForMonthResponse(response.getTotalHours(),
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setSyncStatus(determineSyncStatusForDay(dayInfo.getDate())))
            .toList());
  }

  public WorklogListResponse processWorklogDtos(WorklogListResponse response) {
    final int offset = props.getTimeConfig().getOffset();
    List<WorklogListResponse.WorklogDto> worklogDtos = response.getItems();
    return new WorklogListResponse(
        worklogDtos.stream().peek(worklogDto -> worklogDto.setSyncStatus(
            getSyncStatus(
                TimeLogUtils.getProcessedDate(worklogDto.getDate(), worklogDto.getStartTime(), offset),
                worklogDto.getTicket(),
                worklogDto.getComment())
        )).toList()
    );
  }

  private Status getSyncStatus(final LocalDate date, final String ticket, final String description) {
    List<TimeLogEntity> timeLogEntityList = getTimeLogsForDay(date);
    List<WorklogEntity> worklogEntityList = getWorklogsForDay(date);
    timeLogEntityList = timeLogEntityList
        .stream()
        .filter(entity -> LogSyncUtil.areDescriptionsEqual(entity.getDescription(), description))
        .filter(entity -> Objects.equals(entity.getTicket(), ticket))
        .toList();

    worklogEntityList = worklogEntityList
        .stream()
        .filter(entity -> LogSyncUtil.areDescriptionsEqual(entity.getComment(), description))
        .filter(entity -> Objects.equals(entity.getTicket(), ticket))
        .toList();

    boolean isCompatibleInTime = LogSyncUtil.isWorklogsAndTimeLogsCompatibleInTime(timeLogEntityList, worklogEntityList);
    if (isCompatibleInTime) {
      return Status.SYNCED;
    } else if (!timeLogEntityList.isEmpty() && !worklogEntityList.isEmpty()) {
      return Status.PARTIAL_SYNCED;
    } else {
      return Status.NOT_SYNCED;
    }
  }

  private List<TimeLogEntity> getTimeLogsForDay(final LocalDate date) {
    final int offset = props.getTimeConfig().getOffset();
    return timeLogService.getAllTimeLogEntitiesInMode("Day", date, offset);
  }

  private List<WorklogEntity> getWorklogsForDay(final LocalDate date) {
    final int offset = props.getTimeConfig().getOffset();
    return worklogService.getAllWorklogEntitiesInMode("Day", date, offset);
  }

  private boolean hasTimeLogsSyncStatusForDay(LocalDate date, Status status) {
    final int offset = props.getTimeConfig().getOffset();
    return getTimeLogsForDay(date).stream().anyMatch(
        timeLogEntity -> getSyncStatus(
            TimeLogUtils.getProcessedDate(timeLogEntity.getDate(), timeLogEntity.getStartTime(), offset),
            timeLogEntity.getTicket(),
            timeLogEntity.getDescription()
        ).equals(status)
    );
  }

  private boolean hasWorklogsSyncStatusForDay(LocalDate date, Status status) {
    final int offset = props.getTimeConfig().getOffset();
    return getWorklogsForDay(date).stream().anyMatch(
        worklogEntity -> getSyncStatus(
            TimeLogUtils.getProcessedDate(worklogEntity.getDate(), worklogEntity.getStartTime(), offset),
            worklogEntity.getTicket(),
            worklogEntity.getComment()
        ).equals(status)
    );
  }

}
