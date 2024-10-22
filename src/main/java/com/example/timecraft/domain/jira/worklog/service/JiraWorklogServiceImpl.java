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
import com.example.timecraft.domain.sync.jira.model.SyncJiraProgress;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JiraWorklogServiceImpl implements JiraWorklogService {
  private final AppProperties props;
  private final JiraWorklogHttpClient jiraWorklogHttpClient;
  private final JiraWorklogMapper worklogMapper;
  private final SyncJiraProgress syncJiraProgress;

  @Override
  public List<JiraWorklogDto> listAll() {
    try {
      final List<JiraWorklogDto> allWorklogDtos = new ArrayList<>();
      final List<IssueDto> issues = fetchAllIssues();

      double progressStep = 100.0 / issues.size();

      for (IssueDto issue : issues) {
        final String issueKey = issue.getKey();
        final List<JiraWorklogDto> worklogsForIssue = listForIssue(issueKey);
        allWorklogDtos.addAll(worklogsForIssue);

        updateProgressStatus(issue, progressStep, worklogsForIssue);
      }

      return allWorklogDtos;
    } catch (Exception e) {
      syncJiraProgress.clearProgress();
      throw new RuntimeException("Error fetching all worklogs", e);
    }
  }

  private List<IssueDto> fetchAllIssues() {
    final List<IssueDto> issues = new ArrayList<>();
    int startAt = 0;
    final int maxResults = 100;
    int total;

    do {
      final JiraSearchResponse jiraResponse = jiraWorklogHttpClient.searchIssues(startAt, maxResults);
      if (jiraResponse != null && jiraResponse.getIssues() != null) {
        issues.addAll(jiraResponse.getIssues());
        total = jiraResponse.getTotal();
        syncJiraProgress.setTotalIssues(total);
      } else {
        break;
      }

      startAt += maxResults;
    } while (startAt < total);

    return issues;
  }

  private void updateProgressStatus(final IssueDto issue, final double step, final List<JiraWorklogDto> worklogsForKey) {
    final int workingDayDurationInHours = props.getConfig().getWorkingDayDurationInHours();

    syncJiraProgress.setProgress(syncJiraProgress.getProgress() + step);
    syncJiraProgress.setCurrentIssueNumber(syncJiraProgress.getCurrentIssueNumber() + 1);
    syncJiraProgress.setTotalEstimate(syncJiraProgress.getTotalEstimate() + issue.getFields().getTimeoriginalestimate() * (24 / workingDayDurationInHours));
    syncJiraProgress.setTotalTimeSpent(syncJiraProgress.getTotalTimeSpent() + issue.getFields().getTimespent() * (24 / workingDayDurationInHours));
    final List<SyncJiraProgressResponse.WorklogInfo> worklogInfos = new ArrayList<>();
    for (JiraWorklogDto dto : worklogsForKey) {
      worklogInfos.add(new SyncJiraProgressResponse.WorklogInfo(dto.getDate(), dto.getIssueKey(), dto.getComment()));
    }
    syncJiraProgress.setWorklogInfos(worklogInfos);
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
