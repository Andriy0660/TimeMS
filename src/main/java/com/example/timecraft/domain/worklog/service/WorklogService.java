package com.example.timecraft.domain.worklog.service;

import com.example.timecraft.domain.worklog.dto.WorklogProgressResponse;

public interface WorklogService {
  void synchronizeWorklogs();

  void synchronizeWorklogsForIssue(final String issueKey);

  WorklogProgressResponse getProgress();
}
