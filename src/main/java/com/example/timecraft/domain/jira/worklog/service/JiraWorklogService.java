package com.example.timecraft.domain.jira.worklog.service;

import java.util.List;

import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogCreateDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogUpdateDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;

public interface JiraWorklogService {
  List<JiraWorklogDto> listAll();

  List<JiraWorklogDto> listForIssue(final String issueKey);

  JiraWorklogDto create(final String issueKey, final JiraWorklogCreateDto dto);

  JiraWorklogDto update(final String issueKey, final Long id, final JiraWorklogUpdateDto dto);

  void delete(final String issueKey, final Long id);
}
