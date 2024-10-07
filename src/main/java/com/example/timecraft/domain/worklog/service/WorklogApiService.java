package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;

import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;

public interface WorklogApiService {
  WorklogListResponse list(final String mode, final LocalDate date);

  WorklogCreateFromTimeLogResponse createFromTimeLog(WorklogCreateFromTimeLogRequest request);

  void delete(final String issueKey, final Long id);

}
