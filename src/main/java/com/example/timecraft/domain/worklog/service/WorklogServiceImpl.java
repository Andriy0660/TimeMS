package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogCreateDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.service.JiraWorklogService;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.timelog.util.DurationUtils;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.mapper.WorklogMapper;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorklogServiceImpl implements WorklogService {
  private final WorklogRepository repository;
  private final JiraWorklogService jiraWorklogService;
  private final WorklogMapper mapper;

  @Override
  public WorklogListResponse list(final LocalDate date) {
    final List<WorklogEntity> worklogEntityList = repository.findAllByDate(date);
    final List<WorklogListResponse.WorklogDto> timeLogDtoList = worklogEntityList.stream()
        .map(mapper::toListItem)
        .sorted(Comparator
            .comparing(WorklogListResponse.WorklogDto::getDate)
            .thenComparing(WorklogListResponse.WorklogDto::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
    return new WorklogListResponse(timeLogDtoList);
  }

  @Override
  public WorklogCreateFromTimeLogResponse createFromTimeLog(final WorklogCreateFromTimeLogRequest request) {
    final LocalDateTime dateTime = LocalDateTime.of(request.getDate(), LocalTime.of(10, 0));
    final JiraWorklogDto created = jiraWorklogService.create(request.getTicket(), JiraWorklogCreateDto.builder()
        .started(JiraWorklogUtils.getJiraStartedTime(dateTime))
        .comment(JiraWorklogUtils.getJiraComment(request.getDescription()))
        .timeSpentSeconds((int) DurationUtils.getDurationBetweenStartAndEndTime(request.getStartTime(), request.getEndTime()).toSeconds())
        .build());

    final WorklogEntity entity = mapper.toWorklogEntity(created);
    repository.save(entity);
    return mapper.toCreateResponse(entity);
  }

  @Override
  public void delete(final String ticket, final Long id) {
    try {
      jiraWorklogService.delete(ticket, id);
      repository.deleteById(id);
    } catch (NotFoundException e) {
      throw new BadRequestException("Worklog is already deleted from jira. Please synchronize worklogs for this ticket");
    }
  }

}
