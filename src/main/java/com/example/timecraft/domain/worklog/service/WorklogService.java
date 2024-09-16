package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;
import java.util.List;

import com.example.timecraft.domain.worklog.dto.WorklogProgressResponse;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

public interface WorklogService {
  void synchronizeWorklogs();

  void synchronizeWorklogsForIssue(final String issueKey);

  WorklogProgressResponse getProgress();

  List<WorklogEntity> getAllWorklogEntitiesInMode(final String mode, final LocalDate date, final int offset);
}
