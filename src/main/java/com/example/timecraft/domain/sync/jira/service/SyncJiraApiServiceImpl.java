package com.example.timecraft.domain.sync.jira.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogUpdateDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.service.JiraWorklogService;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncJiraProgressResponse;
import com.example.timecraft.domain.sync.jira.model.SyncJiraProgress;
import com.example.timecraft.domain.timelog.mapper.TimeLogMapper;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.service.TimeLogSyncService;
import com.example.timecraft.domain.timelog.util.DurationUtils;
import com.example.timecraft.domain.worklog.mapper.WorklogMapper;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.service.WorklogSyncService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncJiraApiServiceImpl implements SyncJiraApiService {
  private final TimeLogSyncService timeLogSyncService;
  private final WorklogSyncService worklogSyncService;
  private final JiraWorklogService jiraWorklogService;
  private final SyncJiraProgress syncJiraProgress;
  private final TimeLogMapper timeLogMapper;
  private final WorklogMapper worklogMapper;
  private final Clock clock;

  @Override
  public void syncFromJira(final SyncFromJiraRequest request) {
    final LocalDate date = request.getDate();
    final String description = request.getDescription();
    final String ticket = request.getTicket();

    final List<TimeLogEntity> timeLogEntityList = timeLogSyncService.getAllByDateAndDescriptionAndTicket(date, description, ticket);
    final List<WorklogEntity> worklogEntityList = worklogSyncService.getAllByDateAndCommentAndTicket(date, description, ticket);

    timeLogSyncService.delete(timeLogEntityList);
    timeLogSyncService.saveAll(worklogEntityList.stream().map(worklogEntity -> {
      TimeLogEntity entity = timeLogMapper.worklogToTimeLog(worklogEntity);
      entity.setEndTime(worklogEntity.getStartTime().plusSeconds(worklogEntity.getTimeSpentSeconds()));
      return entity;
    }).toList());
  }

  @Override
  public void syncIntoJira(final SyncIntoJiraRequest request) {
    final LocalDate date = request.getDate();
    final LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(10, 0));
    final String description = request.getDescription();
    final String ticket = request.getTicket();

    final List<TimeLogEntity> timeLogEntityList = timeLogSyncService.getAllByDateAndDescriptionAndTicket(date, description, ticket);
    final List<WorklogEntity> worklogEntityList = worklogSyncService.getAllByDateAndCommentAndTicket(date, description, ticket);

    final int totalSpentSeconds = getTotalSpentSeconds(timeLogEntityList);

    if (worklogEntityList.isEmpty()) {
      throw new BadRequestException("There is no worklog associated with the given ticket");
    }

    deleteWorklogsForTicket(worklogEntityList.stream().skip(1).toList(), ticket);
    final JiraWorklogDto updated = jiraWorklogService.update(ticket, worklogEntityList.getFirst().getId(), JiraWorklogUpdateDto.builder()
        .started(JiraWorklogUtils.getJiraStartedTime(dateTime))
        .timeSpentSeconds(totalSpentSeconds)
        .build());

    WorklogEntity entity = worklogMapper.toWorklogEntity(updated);
    worklogSyncService.save(entity);
  }

  private int getTotalSpentSeconds(final List<TimeLogEntity> timeLogEntityList) {
    return (int) timeLogEntityList.stream().map(timeLogEntity -> {
      if (timeLogEntity.getStartTime() == null || timeLogEntity.getEndTime() == null) return Duration.ZERO;
      return Duration.between(timeLogEntity.getStartTime(), timeLogEntity.getEndTime());
    }).reduce(Duration.ZERO, Duration::plus).toSeconds();
  }

  private void deleteWorklogsForTicket(final List<WorklogEntity> worklogEntityList, final String ticket) {
    try {
      worklogEntityList.forEach(worklog -> jiraWorklogService.delete(ticket, worklog.getId()));
    } catch (HttpClientErrorException.NotFound e) {
      throw new BadRequestException("Synchronization mismatch");
    } finally {
      syncWorklogsForTicket(ticket);
    }
  }

  @Override
  public SyncJiraProgressResponse getProgress() {
    Duration duration = Duration.ZERO;
    if (syncJiraProgress.getStartTime() != null) {
      if (syncJiraProgress.getEndTime() != null) {
        duration = Duration.between(syncJiraProgress.getStartTime(), syncJiraProgress.getEndTime());
      } else {
        duration = Duration.between(syncJiraProgress.getStartTime(), LocalDateTime.now(clock));
      }
    }

    return SyncJiraProgressResponse.builder()
        .isInProgress(syncJiraProgress.isInProgress())
        .progress(syncJiraProgress.getProgress())
        .worklogInfos(syncJiraProgress.getWorklogInfos())
        .duration(DurationUtils.formatDurationHMS(duration))
        .lastSyncedAt(syncJiraProgress.getEndTime())
        .totalIssues(syncJiraProgress.getTotalIssues())
        .currentIssueNumber(syncJiraProgress.getCurrentIssueNumber())
        .totalTimeSpent(DurationUtils.formatDurationDH(Duration.ofSeconds(syncJiraProgress.getTotalTimeSpent())))
        .totalEstimate(DurationUtils.formatDurationDH(Duration.ofSeconds(syncJiraProgress.getTotalEstimate())))
        .build();
  }

  @Override
  public void syncAllWorklogs() {
    if (syncJiraProgress.isInProgress()) return;
    syncJiraProgress.clearProgress();

    syncJiraProgress.setStartTime(LocalDateTime.now(clock));
    syncJiraProgress.setIsInProgress(true);

    final List<WorklogEntity> currentWorklogs = worklogSyncService.getAll();
    final List<WorklogEntity> worklogEntitiesFromJira = jiraWorklogService.listAll()
        .stream()
        .map(worklogMapper::toWorklogEntity)
        .toList();

    syncWorklogs(currentWorklogs, worklogEntitiesFromJira);
    syncJiraProgress.setEndTime(LocalDateTime.now(clock));
    syncJiraProgress.setIsInProgress(false);
  }

  @Override
  public void syncWorklogsForTicket(final String ticket) {
    final List<WorklogEntity> currentWorklogs = worklogSyncService.getAllByTicket(ticket);
    final List<WorklogEntity> worklogEntitiesFromJira = jiraWorklogService.listForIssue(ticket)
        .stream()
        .map(worklogMapper::toWorklogEntity)
        .toList();

    syncWorklogs(currentWorklogs, worklogEntitiesFromJira);
  }

  private void syncWorklogs(final List<WorklogEntity> currentWorklogs, final List<WorklogEntity> worklogEntitiesFromJira) {
    for (WorklogEntity worklogEntityFromJira : worklogEntitiesFromJira) {
      final Optional<WorklogEntity> worklogOpt = worklogSyncService.getById(worklogEntityFromJira.getId());
      if (worklogOpt.isEmpty()
          || worklogOpt.get().getUpdated() == null
          || worklogOpt.get().getUpdated().isBefore(worklogEntityFromJira.getUpdated())) {
        worklogSyncService.save(worklogEntityFromJira);
      }
    }
    final Set<Long> jiraWorklogIds = worklogEntitiesFromJira.stream()
        .map(WorklogEntity::getId)
        .collect(Collectors.toSet());

    for (WorklogEntity worklogEntity : currentWorklogs) {
      if (!jiraWorklogIds.contains(worklogEntity.getId())) {
        worklogSyncService.delete(worklogEntity);
      }
    }
  }

}
