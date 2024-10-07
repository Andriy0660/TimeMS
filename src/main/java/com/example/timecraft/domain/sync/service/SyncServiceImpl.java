package com.example.timecraft.domain.sync.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.jira.worklog.dto.JiraUpdateWorklogDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.service.JiraWorklogService;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.sync.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.model.Status;
import com.example.timecraft.domain.sync.util.SyncUtil;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.service.TimeLogService;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.mapper.WorklogMapper;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.service.WorklogService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {
  private final TimeLogService timeLogService;
  private final WorklogService worklogService;
  private final JiraWorklogService jiraWorklogService;
  private final WorklogMapper worklogMapper;
  private final AppProperties props;

  @Override
  public void syncFromJira(final SyncFromJiraRequest request) {
    List<TimeLogEntity> timeLogEntityList = getTimeLogsForDay(request.getDate());
    timeLogEntityList = timeLogEntityList
        .stream()
        .filter(entity -> SyncUtil.areDescriptionsEqual(entity.getDescription(), request.getDescription()))
        .filter(entity -> Objects.equals(entity.getTicket(), request.getTicket()))
        .toList();
    timeLogService.delete(timeLogEntityList);

    List<WorklogEntity> worklogEntityList = getWorklogsForDay(request.getDate());
    worklogEntityList = worklogEntityList
        .stream()
        .filter(entity -> SyncUtil.areDescriptionsEqual(entity.getComment(), request.getDescription()))
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

  @Override
  public void syncIntoJira(final SyncIntoJiraRequest request) {
    final int offset = props.getTimeConfig().getOffset();
    LocalDateTime dateTime = LocalDateTime.of(request.getDate(), LocalTime.of(10, 0));
    List<WorklogEntity> worklogEntities = worklogService.getAllWorklogEntitiesInMode("Day", request.getDate(), offset);
    worklogEntities = worklogEntities
        .stream()
        .filter(entity -> SyncUtil.areDescriptionsEqual(entity.getComment(), request.getDescription()))
        .filter(entity -> Objects.equals(entity.getTicket(), request.getTicket()))
        .toList();

    int worklogCount = worklogEntities.size();
    if (worklogCount == 0) throw new BadRequestException("There is no worklog associated with the given ticket");
    try {
      if (worklogCount > 1) {
        for (int i = 1; i < worklogCount; i++) {
          jiraWorklogService.delete(request.getTicket(), worklogEntities.get(i).getId());
        }
      }
    } catch (HttpClientErrorException.NotFound e) {
      throw new BadRequestException("Synchronization mismatch");
    } finally {
      worklogService.syncWorklogsForIssue(request.getTicket());
    }


    final Long id = worklogEntities.getFirst().getId();
    JiraWorklogDto updated = jiraWorklogService.update(request.getTicket(), id, JiraUpdateWorklogDto.builder()
        .started(JiraWorklogUtils.getJiraStartedTime(dateTime))
        .timeSpentSeconds(request.getTotalSpent())
        .build());

    WorklogEntity entity = worklogMapper.toWorklogEntity(updated);
    worklogService.save(entity);
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
        .filter(entity -> SyncUtil.areDescriptionsEqual(entity.getDescription(), description))
        .filter(entity -> Objects.equals(entity.getTicket(), ticket))
        .toList();

    worklogEntityList = worklogEntityList
        .stream()
        .filter(entity -> SyncUtil.areDescriptionsEqual(entity.getComment(), description))
        .filter(entity -> Objects.equals(entity.getTicket(), ticket))
        .toList();

    boolean isCompatibleInTime = SyncUtil.isWorklogsAndTimeLogsCompatibleInTime(timeLogEntityList, worklogEntityList);
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
