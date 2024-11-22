package com.example.timecraft.domain.jira.worklog.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.domain.jira.worklog.dto.IssueDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraSearchResponse;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogCreateDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogUpdateDto;
import com.example.timecraft.domain.jira.worklog.mapper.JiraWorklogMapper;
import com.example.timecraft.domain.sync.jira.dto.SyncJiraProgressResponse;
import com.example.timecraft.domain.sync.jira.model.SyncUserJiraProgress;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JiraWorklogServiceImpl implements JiraWorklogService {
  private final AppProperties props;
  private final JiraWorklogHttpClient jiraWorklogHttpClient;
  private final JiraWorklogMapper worklogMapper;

  @Override
  public List<JiraWorklogDto> listAll(final SyncUserJiraProgress syncUserJiraProgress) {
    final List<JiraWorklogDto> allWorklogDtos = new ArrayList<>();
    final List<IssueDto> issues = fetchAllIssues(syncUserJiraProgress);

    double progressStep = 100.0 / issues.size();

    for (IssueDto issue : issues) {
      final String issueKey = issue.getKey();
      final List<JiraWorklogDto> worklogsForIssue = listForIssue(issueKey);
      allWorklogDtos.addAll(worklogsForIssue);

      updateProgressStatus(syncUserJiraProgress, issue, progressStep, worklogsForIssue);
    }

    return allWorklogDtos;
  }

  private List<IssueDto> fetchAllIssues(final SyncUserJiraProgress syncUserJiraProgress) {
    final List<IssueDto> issues = new ArrayList<>();
    int startAt = 0;
    final int maxResults = 100;
    int total;

    do {
      final JiraSearchResponse jiraResponse = jiraWorklogHttpClient.searchIssues(startAt, maxResults);
      if (jiraResponse != null && jiraResponse.getIssues() != null) {
        issues.addAll(jiraResponse.getIssues());
        total = jiraResponse.getTotal();
        syncUserJiraProgress.setTotalIssues(total);
      } else {
        break;
      }

      startAt += maxResults;
    } while (startAt < total);

    return issues;
  }

  private void updateProgressStatus(final SyncUserJiraProgress syncUserJiraProgress, final IssueDto issue, final double step, final List<JiraWorklogDto> worklogsForKey) {
    final int workingDayDurationInHours = 8;

    syncUserJiraProgress.setProgress(syncUserJiraProgress.getProgress() + step);
    syncUserJiraProgress.setCurrentIssueNumber(syncUserJiraProgress.getCurrentIssueNumber() + 1);
    syncUserJiraProgress.setTotalEstimate(syncUserJiraProgress.getTotalEstimate() + issue.getFields().getTimeoriginalestimate() * (24 / workingDayDurationInHours));
    syncUserJiraProgress.setTotalTimeSpent(syncUserJiraProgress.getTotalTimeSpent() + issue.getFields().getTimespent() * (24 / workingDayDurationInHours));
    final List<SyncJiraProgressResponse.WorklogInfo> worklogInfos = new ArrayList<>();
    for (JiraWorklogDto dto : worklogsForKey) {
      worklogInfos.add(new SyncJiraProgressResponse.WorklogInfo(dto.getDate(), dto.getIssueKey(), dto.getComment()));
    }
    syncUserJiraProgress.setWorklogInfos(worklogInfos);
  }

  @Override
  public List<JiraWorklogDto> listForIssue(final String issueKey) {
    final String accountId = worklogMapper.parseJiraAccountId(jiraWorklogHttpClient.getJiraAccountId());
    final String jsonResponse = jiraWorklogHttpClient.getWorklogsForIssue(issueKey);
    return worklogMapper.parseWorklogs(jsonResponse, issueKey, accountId);
  }

  @Override
  public JiraWorklogDto create(final String issueKey, final JiraWorklogCreateDto createDto) {
    final String worklogJson = jiraWorklogHttpClient.createWorklog(issueKey, createDto);
    return worklogMapper.parseWorklogResponse(worklogJson, issueKey);
  }

  @Override
  public JiraWorklogDto update(final String issueKey, final Long id, final JiraWorklogUpdateDto updateDto) {
    final String worklogJson = jiraWorklogHttpClient.updateWorklog(issueKey, id, updateDto);
    return worklogMapper.parseWorklogResponse(worklogJson, issueKey);
  }

  @Override
  public void delete(final String issueKey, final Long id) {
    jiraWorklogHttpClient.deleteWorklog(issueKey, id);
  }

}
