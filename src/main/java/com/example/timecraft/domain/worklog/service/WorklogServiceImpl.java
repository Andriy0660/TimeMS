package com.example.timecraft.domain.worklog.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.utils.TimeLogUtils;
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

  private static boolean areCommentsEqual(final WorklogEntity worklogEntity, final TimeLogEntity timeLogEntity) {
    final String worklogComment = worklogEntity.getComment() != null ? removeNonLetterCharacters(worklogEntity.getComment()) : null;
    final String timeLogDescription = timeLogEntity.getDescription() != null ? removeNonLetterCharacters(timeLogEntity.getDescription().trim()) : null;
    if (worklogComment == null && timeLogDescription == null) {
      return true;
    }
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

  public void synchronizeWorklogs() {
    if (syncProgressService.getProgress() > 0) return;
    worklogRepository.saveAll(jiraWorklogService.fetchAllWorkLogDtos()
        .stream()
        .map(mapper::toWorklogEntity)
        .toList());
    syncProgressService.clearProgress();
  }

  public boolean isWorklogCompatibleWithTimelog(final WorklogEntity worklogEntity, final TimeLogEntity timeLogEntity) {
    return worklogEntity.getDate().equals(timeLogEntity.getDate())
        && areCommentsEqual(worklogEntity, timeLogEntity)
        && areDurationsEqual(worklogEntity, timeLogEntity);
  }

  private boolean areDurationsEqual(final WorklogEntity worklogEntity, final TimeLogEntity timeLogEntity) {
    int duration = (int) Duration.between(timeLogEntity.getStartTime(), timeLogEntity.getEndTime()).toSeconds();
    duration = duration < 0 ? 3600 * 24 + duration : duration;
    return worklogEntity.getTimeSpentSeconds().equals(duration);
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
}
