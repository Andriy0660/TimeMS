package com.example.timecraft.domain.sync.jira.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.jira.worklog.dto.JiraUpdateWorklogDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.service.JiraWorklogService;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.timelog.mapper.TimeLogMapper;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.service.TimeLogService;
import com.example.timecraft.domain.worklog.mapper.WorklogMapper;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.service.WorklogService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncJiraApiServiceImpl implements SyncJiraApiService {
  private final TimeLogService timeLogService;
  private final WorklogService worklogService;
  private final JiraWorklogService jiraWorklogService;
  private final TimeLogMapper timeLogMapper;
  private final WorklogMapper worklogMapper;

  @Override
  public void syncFromJira(final SyncFromJiraRequest request) {
    LocalDate date = request.getDate();
    String description = request.getDescription();
    String ticket = request.getTicket();

    List<TimeLogEntity> timeLogEntityList = timeLogService.findAllByDateAndDescriptionAndTicket(date, description, ticket);
    List<WorklogEntity> worklogEntityList = worklogService.getAllByDateAndCommentAndTicket(date, description, ticket);

    timeLogService.delete(timeLogEntityList);
    timeLogService.saveAll(worklogEntityList.stream().map(worklogEntity -> {
      TimeLogEntity entity = timeLogMapper.worklogToTimeLog(worklogEntity);
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

}
