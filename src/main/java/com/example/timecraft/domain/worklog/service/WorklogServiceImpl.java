package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

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
}
