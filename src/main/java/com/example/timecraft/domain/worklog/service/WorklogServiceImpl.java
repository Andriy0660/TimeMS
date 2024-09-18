package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.atlassian.adf.model.node.Doc;
import com.atlassian.adf.model.node.Paragraph;
import com.example.timecraft.domain.jira.worklog.dto.JiraCreateWorklogDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.service.JiraWorklogService;
import com.example.timecraft.domain.timelog.utils.TimeLogUtils;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogProgressResponse;
import com.example.timecraft.domain.worklog.mapper.WorklogMapper;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.atlassian.adf.model.node.Doc.doc;
import static com.atlassian.adf.model.node.Paragraph.p;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorklogServiceImpl implements WorklogService {
  private final WorklogRepository worklogRepository;
  private final JiraWorklogService jiraWorklogService;
  private final SyncProgressService syncProgressService;
  private final WorklogMapper mapper;
  public static final DateTimeFormatter JIRA_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  @Override
  public void synchronizeWorklogs() {
    if (syncProgressService.getProgress() > 0) return;
    worklogRepository.deleteAll();
    worklogRepository.saveAll(jiraWorklogService.fetchAllWorkLogDtos()
        .stream()
        .map(mapper::toWorklogEntity)
        .toList());
    syncProgressService.clearProgress();
  }

  @Override
  public WorklogCreateFromTimeLogResponse createWorklogFromTimeLog(final WorklogCreateFromTimeLogRequest request) {
    LocalDateTime dateTime = LocalDateTime.of(request.getDate(), LocalTime.of(10, 0));
    JiraWorklogDto created = jiraWorklogService.create(request.getTicket(), JiraCreateWorklogDto.builder()
              .started(dateTime.atZone(ZoneId.systemDefault()).format(JIRA_DATE_TIME_FORMATTER))
              .comment(getComment(request))
              .timeSpentSeconds(TimeLogUtils.getDurationInSecondsForTimelog(request.getStartTime(), request.getEndTime()))
          .build());

    WorklogEntity entity = mapper.toWorklogEntity(created);
    worklogRepository.save(entity);
    return mapper.toCreateResponse(entity);
  }

  private Map<String, ?> getComment(final WorklogCreateFromTimeLogRequest request) {
    List<String> lines = request.getDescription().lines().toList();
    List<Paragraph> paragraphs = new ArrayList<>();

    for (String line : lines) {
      if (!line.isEmpty()) {
        paragraphs.add(p(line));
      } else {
        paragraphs.add(p());
      }
    }
    Doc commentDoc = doc(paragraphs);
    return commentDoc.toMap();
  }

  @Override
  public void synchronizeWorklogsForIssue(final String issueKey) {
    var list = jiraWorklogService.fetchWorklogDtosForIssue(issueKey)
        .stream()
        .map(mapper::toWorklogEntity)
        .toList();
    worklogRepository.deleteAll(worklogRepository.findAllByTicket(issueKey));
    worklogRepository.saveAll(list);
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
  public void deleteUnsyncedWorklog(final String issueKey, final Long id) {
    jiraWorklogService.delete(issueKey, id);
    synchronizeWorklogsForIssue(issueKey);
  }
}
