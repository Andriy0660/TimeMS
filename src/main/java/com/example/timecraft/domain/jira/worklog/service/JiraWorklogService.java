package com.example.timecraft.domain.jira.worklog.service;

import java.util.List;

import com.example.timecraft.domain.jira.worklog.dto.JiraCreateWorklogDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;

public interface JiraWorklogService {
  List<JiraWorklogDto> fetchAllWorkLogDtos();

  List<JiraWorklogDto> fetchWorklogDtosForIssue(final String issueKey);

  JiraWorklogDto create(final String issueKey, final JiraCreateWorklogDto dto);

  void delete(final String issueKey, final Long id);
}
