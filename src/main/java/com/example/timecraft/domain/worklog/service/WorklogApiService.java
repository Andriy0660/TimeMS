package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;

import com.example.timecraft.domain.timelog.model.ViewMode;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;

public interface WorklogApiService {
  WorklogListResponse list(final LocalDate date);

  WorklogCreateFromTimeLogResponse createFromTimeLog(final WorklogCreateFromTimeLogRequest request);

  void delete(final String ticket, final Long id);

}
