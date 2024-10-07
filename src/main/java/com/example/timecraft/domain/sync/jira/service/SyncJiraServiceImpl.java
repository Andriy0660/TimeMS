package com.example.timecraft.domain.sync.jira.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.jira.worklog.dto.JiraUpdateWorklogDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.service.JiraWorklogService;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.jira.mapper.SyncJiraMapper;
import com.example.timecraft.domain.sync.model.SyncStatus;
import com.example.timecraft.domain.sync.jira.util.SyncJiraUtil;
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
public class SyncJiraServiceImpl implements SyncJiraService {
  private final TimeLogService timeLogService;
  private final WorklogService worklogService;
  private final JiraWorklogService jiraWorklogService;
  private final WorklogMapper worklogMapper;
  private final SyncJiraMapper syncJiraMapper;
  private final AppProperties props;

  @Override
  public void syncFromJira(final SyncFromJiraRequest request) {
    LocalDate date = request.getDate();
    String description = request.getDescription();
    String ticket = request.getTicket();

    List<TimeLogEntity> timeLogEntityList = timeLogService.findAllByDateAndDescriptionAndTicket(date, description, ticket);
    List<WorklogEntity> worklogEntityList = worklogService.getAllByDateAndCommentAndTicket(date, description, ticket);

    timeLogService.delete(timeLogEntityList);
    timeLogService.saveAll(worklogEntityList.stream().map(worklogEntity -> {
      TimeLogEntity entity = syncJiraMapper.worklogToTimeLog(worklogEntity);
      entity.setEndTime(worklogEntity.getStartTime().plusSeconds(worklogEntity.getTimeSpentSeconds()));
      return entity;
    }).toList());
  }

  @Override
  public void syncIntoJira(final SyncIntoJiraRequest request) {
    LocalDate date = request.getDate();
    LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(10, 0));
    String description = request.getDescription();
    String ticket = request.getTicket();

    List<TimeLogEntity> timeLogEntityList = timeLogService.findAllByDateAndDescriptionAndTicket(date, description, ticket);
    List<WorklogEntity> worklogEntityList = worklogService.getAllByDateAndCommentAndTicket(date, description, ticket);

    int totalSpentSeconds = getTotalSpentSeconds(timeLogEntityList);

    deleteAllWorklogsForTicketExceptFirst(worklogEntityList, ticket);

    final Long id = worklogEntityList.getFirst().getId();
    JiraWorklogDto updated = jiraWorklogService.update(ticket, id, JiraUpdateWorklogDto.builder()
        .started(JiraWorklogUtils.getJiraStartedTime(dateTime))
        .timeSpentSeconds(totalSpentSeconds)
        .build());

    WorklogEntity entity = worklogMapper.toWorklogEntity(updated);
    worklogService.save(entity);
  }

  private int getTotalSpentSeconds(final List<TimeLogEntity> timeLogEntityList) {
    return (int) timeLogEntityList.stream().map(timeLogEntity -> {
      if (timeLogEntity.getStartTime() == null || timeLogEntity.getEndTime() == null) return Duration.ZERO;
      return Duration.between(timeLogEntity.getStartTime(), timeLogEntity.getEndTime());
    }).reduce(Duration.ZERO, Duration::plus).toSeconds();
  }

  private void deleteAllWorklogsForTicketExceptFirst(final List<WorklogEntity> worklogEntityList, final String ticket) {
    int worklogCount = worklogEntityList.size();
    if (worklogCount == 0) {
      throw new BadRequestException("There is no worklog associated with the given ticket");
    } else if (worklogCount > 1) {
      try {
        worklogEntityList.stream().skip(1).forEach(worklog -> jiraWorklogService.delete(ticket, worklog.getId()));
      } catch (HttpClientErrorException.NotFound e) {
        throw new BadRequestException("Synchronization mismatch");
      } finally {
        worklogService.syncWorklogsForIssue(ticket);
      }
    }
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

  private SyncStatus determineSyncStatusForDay(LocalDate date) {
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

  private SyncStatus getSyncStatus(final LocalDate date, final String ticket, final String description) {
    List<TimeLogEntity> timeLogEntityList = timeLogService.findAllByDateAndDescriptionAndTicket(date, description, ticket);
    List<WorklogEntity> worklogEntityList = worklogService.getAllByDateAndCommentAndTicket(date, description, ticket);

    boolean isCompatibleInTime = SyncJiraUtil.isWorklogsAndTimeLogsCompatibleInTime(timeLogEntityList, worklogEntityList);
    if (isCompatibleInTime) {
      return SyncStatus.SYNCED;
    } else if (!timeLogEntityList.isEmpty() && !worklogEntityList.isEmpty()) {
      return SyncStatus.PARTIAL_SYNCED;
    } else {
      return SyncStatus.NOT_SYNCED;
    }
  }

  private boolean hasTimeLogsSyncStatusForDay(LocalDate date, SyncStatus syncStatus) {
    final int offset = props.getTimeConfig().getOffset();
    return getTimeLogsForDay(date).stream().anyMatch(
        timeLogEntity -> getSyncStatus(
            TimeLogUtils.getProcessedDate(timeLogEntity.getDate(), timeLogEntity.getStartTime(), offset),
            timeLogEntity.getTicket(),
            timeLogEntity.getDescription()
        ).equals(syncStatus)
    );
  }

  private List<TimeLogEntity> getTimeLogsForDay(final LocalDate date) {
    final int offset = props.getTimeConfig().getOffset();
    return timeLogService.getAllTimeLogEntitiesInMode("Day", date, offset);
  }

  private boolean hasWorklogsSyncStatusForDay(LocalDate date, SyncStatus syncStatus) {
    return worklogService.getAllByDate(date).stream()
        .anyMatch(
          worklogEntity -> getSyncStatus(worklogEntity.getDate(), worklogEntity.getTicket(), worklogEntity.getComment()
        ).equals(syncStatus));
  }

}
