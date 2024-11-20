package com.example.timecraft.domain.jira.worklog.service;

import java.util.List;

import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogCreateDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogUpdateDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.sync.jira.model.SyncUserJiraProgress;

public interface JiraWorklogService {
  List<JiraWorklogDto> listAll(final SyncUserJiraProgress syncUserJiraProgress);

  List<JiraWorklogDto> listForIssue(final String issueKey);

  JiraWorklogDto create(final String issueKey, final JiraWorklogCreateDto dto);

  JiraWorklogDto update(final String issueKey, final Long id, final JiraWorklogUpdateDto dto);

  void delete(final String issueKey, final Long id);
}
