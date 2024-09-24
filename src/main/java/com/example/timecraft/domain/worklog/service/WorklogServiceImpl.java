package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.jira.worklog.dto.JiraCreateWorklogDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.service.JiraWorklogService;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
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
  private final AppProperties props;

  @Override
  public WorklogListResponse list(final String mode, final LocalDate date) {
    final int offset = props.getTimeConfig().getOffset();
    List<WorklogEntity> worklogEntityList = getAllWorklogEntitiesInMode(mode, date, offset);
    final List<WorklogListResponse.WorklogDto> timeLogDtoList = worklogEntityList.stream()
        .map(mapper::toListItem)
        .toList();
    return new WorklogListResponse(timeLogDtoList);
  }

  @Override
  public List<WorklogEntity> getAllWorklogEntitiesInMode(final String mode, final LocalDate date, final int offset) {
    final LocalTime startTime = LocalTime.of(offset, 0);
    final LocalDate[] dateRange = TimeLogUtils.calculateDateRange(mode, date);

    if ("All".equals(mode)) {
      return worklogRepository.findAll();
    } else {
      return worklogRepository.findAllInRange(dateRange[0], dateRange[1], startTime);
    }
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
    return new WorklogProgressResponse(
        syncProgressService.getProgress(),
        syncProgressService.getTicketOfCurrentWorklog(),
        syncProgressService.getCommentOfCurrentWorklog()
    );
  }

  @Override
  public void syncWorklogs() {
    if (syncProgressService.getProgress() > 0) return;

    List<WorklogEntity> currentWorklogs = worklogRepository.findAll();
    List<WorklogEntity> worklogEntitiesFromJira = jiraWorklogService.fetchAllWorkLogDtos()
        .stream()
        .map(mapper::toWorklogEntity)
        .toList();

    syncWorklogs(currentWorklogs, worklogEntitiesFromJira);

    syncProgressService.clearProgress();
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
