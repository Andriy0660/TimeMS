package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;
import java.util.List;

import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.dto.WorklogProgressResponse;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

public interface WorklogService {
  WorklogListResponse list(final String mode, final LocalDate date);

  List<WorklogEntity> getAllWorklogEntitiesInMode(final String mode, final LocalDate date, final int offset);

  WorklogCreateFromTimeLogResponse createWorklogFromTimeLog(WorklogCreateFromTimeLogRequest request);

  void save(final WorklogEntity entity);

  void deleteUnsyncedWorklog(final String issueKey, final Long id);

  WorklogProgressResponse getProgress();

  void syncWorklogs();

  void syncWorklogsForIssue(final String issueKey);
}
