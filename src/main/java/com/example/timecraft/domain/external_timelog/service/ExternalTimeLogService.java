package com.example.timecraft.domain.external_timelog.service;

import java.time.LocalDate;

import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogRequest;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogResponse;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogListResponse;

public interface ExternalTimeLogService {
  ExternalTimeLogListResponse list(final LocalDate date);

  ExternalTimeLogCreateFromTimeLogResponse createFromTimeLog(final ExternalTimeLogCreateFromTimeLogRequest request);

  void delete(final Long id);

}
