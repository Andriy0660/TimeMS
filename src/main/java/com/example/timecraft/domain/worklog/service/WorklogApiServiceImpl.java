package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.jira.worklog.dto.JiraCreateWorklogDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.service.JiraWorklogService;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.sync.jira.util.SyncJiraUtil;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.mapper.WorklogMapper;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorklogApiServiceImpl implements WorklogApiService {
  private final WorklogRepository repository;
  private final JiraWorklogService jiraWorklogService;
  private final WorklogMapper mapper;

  @Override
  public WorklogListResponse list(final String mode, final LocalDate date) {
    List<WorklogEntity> worklogEntityList = repository.findAllByDate(date);
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
  public WorklogCreateFromTimeLogResponse createFromTimeLog(final WorklogCreateFromTimeLogRequest request) {
    LocalDateTime dateTime = LocalDateTime.of(request.getDate(), LocalTime.of(10, 0));
    JiraWorklogDto created = jiraWorklogService.create(request.getTicket(), JiraCreateWorklogDto.builder()
        .started(JiraWorklogUtils.getJiraStartedTime(dateTime))
        .comment(JiraWorklogUtils.getJiraComment(request.getDescription()))
        .timeSpentSeconds(TimeLogUtils.getDurationInSecondsForTimelog(request.getStartTime(), request.getEndTime()))
        .build());

    WorklogEntity entity = mapper.toWorklogEntity(created);
    repository.save(entity);
    return mapper.toCreateResponse(entity);
  }

  @Override
  public void delete(final String issueKey, final Long id) {
    try {
      jiraWorklogService.delete(issueKey, id);
    } catch (HttpClientErrorException.NotFound e) {
      throw new BadRequestException("Worklog is already deleted from jira. Please synchronize worklogs for this ticket");
    }
  }

}
