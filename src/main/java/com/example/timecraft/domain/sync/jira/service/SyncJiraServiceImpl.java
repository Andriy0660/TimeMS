package com.example.timecraft.domain.sync.jira.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogUpdateDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.service.JiraWorklogService;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncJiraProgressResponse;
import com.example.timecraft.domain.sync.jira.model.SyncUserJiraProgress;
import com.example.timecraft.domain.sync.jira.util.SyncJiraUtils;
import com.example.timecraft.domain.timelog.mapper.TimeLogMapper;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.api.TimeLogSyncService;
import com.example.timecraft.domain.timelog.util.DurationUtils;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import com.example.timecraft.domain.user.persistence.UserEntity;
import com.example.timecraft.domain.worklog.mapper.WorklogMapper;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.api.WorklogSyncService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SyncJiraServiceImpl implements SyncJiraService {
  private final TimeLogSyncService timeLogSyncService;
  private final WorklogSyncService worklogSyncService;
  private final JiraWorklogService jiraWorklogService;
  private final SyncJiraProgressService syncJiraProgressService;
  private final TimeLogMapper timeLogMapper;
  private final WorklogMapper worklogMapper;
  private final Clock clock;

  @Override
  public void syncFromJira(final SyncFromJiraRequest request) {
    final LocalDate date = request.getDate();
    final String description = request.getDescription();
    final String ticket = request.getTicket();

    final List<WorklogEntity> worklogEntityList = worklogSyncService.getAllByDateAndCommentAndTicket(date, description, ticket);

    timeLogSyncService.saveAll(worklogEntityList.stream().map(worklogEntity -> {
      TimeLogEntity entity = timeLogMapper.worklogToTimeLog(worklogEntity);
      entity.setEndTime(worklogEntity.getStartTime().plusSeconds(worklogEntity.getTimeSpentSeconds()));
      return entity;
    }).toList());
  }

  @Override
  public void syncIntoJira(final SyncIntoJiraRequest request) {
    final LocalDate date = request.getDate();
    final LocalDateTime dateTime = LocalDateTime.of(date, SyncJiraUtils.DEFAULT_WORKLOG_START_TIME);
    final String description = request.getDescription();
    final String ticket = request.getTicket();

    final List<TimeLogEntity> timeLogEntityList = timeLogSyncService.getAllByDateAndDescriptionAndTicket(date, description, ticket);
    final List<WorklogEntity> worklogEntityList = worklogSyncService.getAllByDateAndCommentAndTicket(date, description, ticket);

    final int totalSpentSeconds = TimeLogUtils.getTotalSpentSecondsForTimeLogs(timeLogEntityList);

    if (worklogEntityList.isEmpty()) {
      throw new BadRequestException("There is no worklog associated with the given ticket");
    }

    deleteWorklogs(worklogEntityList.stream().skip(1).toList());
    final JiraWorklogDto updated = jiraWorklogService.update(ticket, worklogEntityList.getFirst().getId(), JiraWorklogUpdateDto.builder()
        .started(JiraWorklogUtils.getJiraStartedTime(dateTime))
        .timeSpentSeconds(totalSpentSeconds)
        .build());

    WorklogEntity entity = worklogMapper.toWorklogEntity(updated);
    worklogSyncService.save(entity);
  }

  private void deleteWorklogs(final List<WorklogEntity> worklogEntityList) {
    try {
      worklogEntityList.forEach(worklog -> {
        jiraWorklogService.delete(worklog.getTicket(), worklog.getId());
        worklogSyncService.delete(worklog);
      });
    } catch (NotFoundException e) {
      throw new BadRequestException("Synchronization mismatch");
    }
  }

  @Override
  public SyncJiraProgressResponse getProgress() {
    final UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    SyncUserJiraProgress syncUserJiraProgress = Objects.requireNonNullElseGet(syncJiraProgressService.getUserProgress(user.getId()), SyncUserJiraProgress::new);

    Duration duration = Duration.ZERO;
    if (syncUserJiraProgress.getStartTime() != null) {
      if (syncUserJiraProgress.getEndTime() != null) {
        duration = Duration.between(syncUserJiraProgress.getStartTime(), syncUserJiraProgress.getEndTime());
      } else {
        duration = Duration.between(syncUserJiraProgress.getStartTime(), LocalDateTime.now(clock));
      }
    }

    return SyncJiraProgressResponse.builder()
        .isInProgress(syncUserJiraProgress.isInProgress())
        .progress(syncUserJiraProgress.getProgress())
        .worklogInfos(syncUserJiraProgress.getWorklogInfos())
        .duration(DurationUtils.formatDurationHMS(duration))
        .lastSyncedAt(syncUserJiraProgress.getEndTime())
        .totalIssues(syncUserJiraProgress.getTotalIssues())
        .currentIssueNumber(syncUserJiraProgress.getCurrentIssueNumber())
        .totalTimeSpent(DurationUtils.formatDurationDH(Duration.ofSeconds(syncUserJiraProgress.getTotalTimeSpent())))
        .totalEstimate(DurationUtils.formatDurationDH(Duration.ofSeconds(syncUserJiraProgress.getTotalEstimate())))
        .build();
  }

  @Override
  public void syncAllWorklogs() {
    final UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    SyncUserJiraProgress syncUserJiraProgress = syncJiraProgressService.createUserProgress(user.getId());

    if (syncUserJiraProgress.isInProgress()) return;

    syncUserJiraProgress.setStartTime(LocalDateTime.now(clock));
    syncUserJiraProgress.setIsInProgress(true);

    final List<WorklogEntity> currentWorklogs = worklogSyncService.getAll();
    final List<WorklogEntity> worklogEntitiesFromJira;
    try {
      worklogEntitiesFromJira = jiraWorklogService.listAll(syncUserJiraProgress)
          .stream()
          .map(worklogMapper::toWorklogEntity)
          .toList();
    } catch (Exception e) {
      syncJiraProgressService.removeUserProgress(user.getId());
      throw new RuntimeException("Error fetching all worklogs", e);
    }

    syncWorklogs(currentWorklogs, worklogEntitiesFromJira);
    syncUserJiraProgress.setEndTime(LocalDateTime.now(clock));
    syncUserJiraProgress.setIsInProgress(false);
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
