package com.example.timecraft.domain.timelog.service;

import java.time.LocalDate;

import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;

public interface TimeLogService {
  TimeLogListResponse list(LocalDate day);

  TimeLogCreateResponse create(final TimeLogCreateRequest request);

  TimeLogGetResponse get(final long logEntryId);

  TimeLogUpdateResponse update(final long logEntryId, final TimeLogUpdateRequest request);

  void delete(final long logEntryId);
}
