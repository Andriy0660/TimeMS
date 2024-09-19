package com.example.timecraft.domain.logsync.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.timecraft.core.config.AppProperties;
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

  public TimeLogListResponse processTimeLogDtos(TimeLogListResponse response) {
    final int offset = props.getTimeConfig().getOffset();
    List<TimeLogListResponse.TimeLogDto> timeLogDtos = response.getItems();
    return new TimeLogListResponse(
        timeLogDtos.stream().peek(timeLogDto ->
            timeLogDto.setSynced(
                isTimeLogSynced(
                    TimeLogUtils.getProcessedDate(timeLogDto.getDate(), timeLogDto.getStartTime(), offset),
                    timeLogDto.getTicket(),
                    timeLogDto.getDescription())
            )
        ).toList());
  }

  public TimeLogHoursForWeekResponse processWeekDayInfos(TimeLogHoursForWeekResponse response) {
    final int offset = props.getTimeConfig().getOffset();
    List<TimeLogHoursForWeekResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForWeekResponse(
        dayInfos.stream().peek(dayInfo -> dayInfo.setSynchronized(
                getTimeLogsForDay(dayInfo.getDate()).stream().anyMatch(
                    timeLogEntity -> !isTimeLogSynced(
                        TimeLogUtils.getProcessedDate(timeLogEntity.getDate(), timeLogEntity.getStartTime(), offset),
                        timeLogEntity.getTicket(),
                        timeLogEntity.getDescription()
                    )
                )
            )
        ).toList());
  }

  public TimeLogHoursForMonthResponse processMonthDayInfos(TimeLogHoursForMonthResponse response) {
    final int offset = props.getTimeConfig().getOffset();
    List<TimeLogHoursForMonthResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForMonthResponse(response.getTotalHours(),
        dayInfos.stream().peek(dayInfo -> dayInfo.setSynchronized(
                getTimeLogsForDay(dayInfo.getStart().toLocalDate()).stream().anyMatch(
                    timeLogEntity -> !isTimeLogSynced(
                        TimeLogUtils.getProcessedDate(timeLogEntity.getDate(), timeLogEntity.getStartTime(), offset),
                        timeLogEntity.getTicket(),
                        timeLogEntity.getDescription()
                    )
                )
            )
        ).toList());
  }

  public WorklogListResponse processWorklogDtos(WorklogListResponse response) {
    List<WorklogListResponse.WorklogDto> worklogDtos = response.getItems();
    return new WorklogListResponse(
        worklogDtos.stream().peek(worklogDto -> worklogDto.setSynced(
            isWorklogSynced(worklogDto.getDate(), worklogDto.getTicket(), worklogDto.getComment())
        )).toList()
    );
  }

  private boolean isTimeLogSynced(final LocalDate date, final String ticket, final String description) {
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
    return LogSyncUtil.isWorklogsAndTimeLogsCompatibleInTime(timeLogEntityList, worklogEntityList);
  }

  private boolean isWorklogSynced(final LocalDate date, final String ticket, final String comment) {
    List<TimeLogEntity> timeLogEntityList = getTimeLogsForDay(date);
    List<WorklogEntity> worklogEntityList = getWorklogsForDay(date);
    timeLogEntityList = timeLogEntityList
        .stream()
        .filter(entity -> LogSyncUtil.areDescriptionsEqual(entity.getDescription(), comment))
        .filter(entity -> Objects.equals(entity.getTicket(), ticket))
        .toList();

    worklogEntityList = worklogEntityList
        .stream()
        .filter(entity -> LogSyncUtil.areDescriptionsEqual(entity.getComment(), comment))
        .filter(entity -> Objects.equals(entity.getTicket(), ticket))
        .toList();
    return LogSyncUtil.isWorklogsAndTimeLogsCompatibleInTime(timeLogEntityList, worklogEntityList);
  }

  private List<TimeLogEntity> getTimeLogsForDay(final LocalDate date) {
    final int offset = props.getTimeConfig().getOffset();
    return timeLogService.getAllTimeLogEntitiesInMode("Day", date, offset);
  }

  private List<WorklogEntity> getWorklogsForDay(final LocalDate date) {
    final int offset = props.getTimeConfig().getOffset();
    return worklogService.getAllWorklogEntitiesInMode("Day", date, offset);
  }
}
