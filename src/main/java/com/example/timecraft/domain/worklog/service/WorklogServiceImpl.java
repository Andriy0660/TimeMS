package com.example.timecraft.domain.worklog.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.jira.worklog.dto.JiraCreateWorklogDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.service.JiraWorklogService;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.sync.jira.util.SyncJiraUtil;
import com.example.timecraft.domain.timelog.service.DurationService;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.dto.WorklogProgressResponse;
import com.example.timecraft.domain.worklog.mapper.WorklogMapper;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorklogServiceImpl implements WorklogService {
  private final WorklogRepository worklogRepository;
  private final JiraWorklogService jiraWorklogService;
  private final SyncProgressService syncProgressService;
  private final WorklogMapper mapper;
  private final Clock clock;

  @Override
  public WorklogListResponse list(final String mode, final LocalDate date) {
    List<WorklogEntity> worklogEntityList = getAllByDate(date);
    final List<WorklogListResponse.WorklogDto> timeLogDtoList = worklogEntityList.stream()
        .map(worklogEntity -> {
          WorklogListResponse.WorklogDto worklogDto = mapper.toListItem(worklogEntity);
          worklogDto.setColor(TimeLogUtils.generateColor(
              worklogEntity.getTicket(),
              SyncJiraUtil.removeNonLetterAndDigitCharacters(worklogEntity.getComment())
          ));
          return worklogDto;
        })
        .sorted(Comparator
            .comparing(WorklogListResponse.WorklogDto::getDate)
            .thenComparing(WorklogListResponse.WorklogDto::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
    return new WorklogListResponse(timeLogDtoList);
  }

  @Override
  public List<WorklogEntity> getAllByDate(final LocalDate date) {
    return worklogRepository.findAllByDate(date);
  }

  @Override
  public List<WorklogEntity> getAllByDateAndCommentAndTicket(final LocalDate date, final String comment, final String ticket) {
    return worklogRepository.findAllByDateAndTicket(date, ticket).stream()
        .filter(worklogEntity -> SyncJiraUtil.areDescriptionsEqual(worklogEntity.getComment(), comment))
        .toList();
  }

  @Override
  public WorklogCreateFromTimeLogResponse createWorklogFromTimeLog(final WorklogCreateFromTimeLogRequest request) {
    LocalDateTime dateTime = LocalDateTime.of(request.getDate(), LocalTime.of(10, 0));
    JiraWorklogDto created = jiraWorklogService.create(request.getTicket(), JiraCreateWorklogDto.builder()
        .started(JiraWorklogUtils.getJiraStartedTime(dateTime))
        .comment(JiraWorklogUtils.getJiraComment(request.getDescription()))
        .timeSpentSeconds(TimeLogUtils.getDurationInSecondsForTimelog(request.getStartTime(), request.getEndTime()))
        .build());

    WorklogEntity entity = mapper.toWorklogEntity(created);
    worklogRepository.save(entity);
    return mapper.toCreateResponse(entity);
  }

  @Override
  public void save(final WorklogEntity entity) {
    worklogRepository.save(entity);
  }

  @Override
  public void deleteUnsyncedWorklog(final String issueKey, final Long id) {
    try {
      jiraWorklogService.delete(issueKey, id);
    } catch (HttpClientErrorException.NotFound e) {
      throw new BadRequestException("Worklog is already deleted from jira");
    } finally {
      syncWorklogsForIssue(issueKey);
    }
  }

  @Override
  public WorklogProgressResponse getProgress() {
    Duration duration = Duration.ZERO;
    if (syncProgressService.getStartTime() != null) {
      if (syncProgressService.getEndTime() != null) {
        duration = Duration.between(syncProgressService.getStartTime(), syncProgressService.getEndTime());
      } else {
        duration = Duration.between(syncProgressService.getStartTime(), LocalDateTime.now(clock));
      }
    }

    return WorklogProgressResponse.builder()
        .isInProgress(syncProgressService.isInProgress())
        .progress(syncProgressService.getProgress())
        .worklogInfos(syncProgressService.getWorklogInfos())
        .duration(DurationService.formatDurationHMS(duration))
        .lastSyncedAt(syncProgressService.getEndTime())
        .totalIssues(syncProgressService.getTotalIssues())
        .currentIssueNumber(syncProgressService.getCurrentIssueNumber())
        .totalTimeSpent(DurationService.formatDurationDH(Duration.ofSeconds(syncProgressService.getTotalTimeSpent())))
        .totalEstimate(DurationService.formatDurationDH(Duration.ofSeconds(syncProgressService.getTotalEstimate())))
        .build();
  }

  @Override
  public void syncWorklogs() {
    if (syncProgressService.isInProgress()) return;
    syncProgressService.clearProgress();

    syncProgressService.setStartTime(LocalDateTime.now(clock));
    syncProgressService.setIsInProgress(true);

    List<WorklogEntity> currentWorklogs = worklogRepository.findAll();
    List<WorklogEntity> worklogEntitiesFromJira = jiraWorklogService.fetchAllWorkLogDtos()
        .stream()
        .map(mapper::toWorklogEntity)
        .toList();

    syncWorklogs(currentWorklogs, worklogEntitiesFromJira);
    syncProgressService.setEndTime(LocalDateTime.now(clock));
    syncProgressService.setIsInProgress(false);
  }

  @Override
  public void syncWorklogsForIssue(final String issueKey) {
    List<WorklogEntity> currentWorklogs = worklogRepository.findAllByTicket(issueKey);
    List<WorklogEntity> worklogEntitiesFromJira = jiraWorklogService.fetchWorklogDtosForIssue(issueKey)
        .stream()
        .map(mapper::toWorklogEntity)
        .toList();

    syncWorklogs(currentWorklogs, worklogEntitiesFromJira);
  }

  private void syncWorklogs(final List<WorklogEntity> currentWorklogs, final List<WorklogEntity> worklogEntitiesFromJira) {
    for (WorklogEntity worklogEntityFromJira : worklogEntitiesFromJira) {
      Optional<WorklogEntity> worklogOpt = worklogRepository.findById(worklogEntityFromJira.getId());
      if (worklogOpt.isEmpty()
          || worklogOpt.get().getUpdated() == null
          || worklogOpt.get().getUpdated().isBefore(worklogEntityFromJira.getUpdated())) {
        worklogRepository.save(worklogEntityFromJira);
      }
    }
    Set<Long> jiraWorklogIds = worklogEntitiesFromJira.stream()
        .map(WorklogEntity::getId)
        .collect(Collectors.toSet());

    for (WorklogEntity worklogEntity : currentWorklogs) {
      if (!jiraWorklogIds.contains(worklogEntity.getId())) {
        worklogRepository.delete(worklogEntity);
      }
    }
  }
}
