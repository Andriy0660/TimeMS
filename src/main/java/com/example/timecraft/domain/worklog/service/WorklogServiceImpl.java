package com.example.timecraft.domain.worklog.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.persistence.TimeLogRepository;
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
  private final TimeLogRepository timeLogRepository;
  private final JiraWorklogService jiraWorklogService;
  private final SyncProgressService syncProgressService;
  private final WorklogMapper mapper;

  public void synchronizeWorklogs() {
    if(syncProgressService.getProgress() > 0) return;
    var list = jiraWorklogService.fetchAllWorkLogDtos()
        .stream()
        .map(mapper::toWorklogEntity)
        .toList();
    worklogRepository.saveAll(list);
    syncProgressService.clearProgress();
    // testing purpose: basic code for 100% comparing
    for (LocalDate date : list.stream().map(WorklogEntity::getDate).distinct().toList()) {
      List<TimeLogEntity> entities = timeLogRepository.findAllByDateIs(date);
      List<WorklogEntity> worklogEntities = worklogRepository.findAllByDateIs(date);
      for (WorklogEntity worklogEntity : worklogEntities) {
        boolean match = false;
        for (TimeLogEntity timeLogEntity : entities) {
          if (isWorklogCompatibleWithTimelog(worklogEntity, timeLogEntity)) {
            match = true;
          }
        }
        if (!match) {
          log.warn("worklog does not match {}", worklogEntity);
        }
      }
    }
  }

  private boolean isWorklogCompatibleWithTimelog(WorklogEntity worklog, TimeLogEntity timeLog) {
    return worklog.getDate().equals(timeLog.getDate())
        && areCommentsEqual(worklog, timeLog)
        && areDurationsEqual(worklog, timeLog);
  }

  private boolean areDurationsEqual(final WorklogEntity worklog, final TimeLogEntity timeLog) {
    int duration = (int) Duration.between(timeLog.getStartTime(), timeLog.getEndTime()).toSeconds();
    duration = duration < 0 ? 3600 * 24 + duration : duration;
    return worklog.getTimeSpentSeconds().equals(duration);
  }

  private static boolean areCommentsEqual(WorklogEntity worklog, TimeLogEntity timeLog) {
    String worklogComment = worklog.getComment() != null ? removeNonLetterCharacters(worklog.getComment()) : null;
    String timeLogDescription = timeLog.getDescription() != null ? removeNonLetterCharacters(timeLog.getDescription().trim()) : null;
    if(worklogComment == null && timeLogDescription == null) return true;
    return worklogComment != null && worklogComment.equals(timeLogDescription);
  }

  private static String removeNonLetterCharacters(String input) {
    StringBuilder result = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isLetter(c)) {
        result.append(c);
      }
    }
    return result.toString();
  }

  @Override
  public void synchronizeWorklogsForIssue(final String issueKey) {
    var list = jiraWorklogService.fetchWorklogDtosForIssue(issueKey)
        .stream()
        .map(mapper::toWorklogEntity)
        .toList();
    worklogRepository.saveAll(list);
  }

  public WorklogProgressResponse getProgress() {
    return new WorklogProgressResponse(
        syncProgressService.getProgress(),
        syncProgressService.getTicketOfCurrentWorklog(),
        syncProgressService.getCommentOfCurrentWorklog()
    );
  }
}
