package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;
import java.util.List;

import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogProgressResponse;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

public interface WorklogService {
  List<WorklogEntity> getAllWorklogEntitiesInMode(final String mode, final LocalDate date, final int offset);

  WorklogCreateFromTimeLogResponse createWorklogFromTimeLog(WorklogCreateFromTimeLogRequest request);

  void deleteUnsyncedWorklog(final String issueKey, final Long id);

  WorklogProgressResponse getProgress();

  void synchronizeWorklogs();

  void synchronizeWorklogsForIssue(final String issueKey);
}
