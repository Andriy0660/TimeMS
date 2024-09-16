package com.example.timecraft.domain.worklog.service;

import java.util.List;

import com.example.timecraft.domain.worklog.dto.WorklogJiraDto;

public interface JiraWorklogService {
  List<WorklogJiraDto> fetchAllWorkLogDtos();

  List<WorklogJiraDto> fetchWorklogDtosForIssue(String issueKey);

  void delete(final String ticket, final Long id);
}
