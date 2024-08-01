package com.example.timecraft.domain.timelog.service;

import java.time.LocalDate;

import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;

public interface TimeLogService {
  TimeLogListResponse list(final String mode, final LocalDate date);

  TimeLogCreateResponse create(final TimeLogCreateRequest request);

  TimeLogGetResponse get(final long timeLogId);

  TimeLogUpdateResponse update(final long timeLogId, final TimeLogUpdateRequest request);

  void delete(final long timeLogId);

  void setGroupDescription(TimeLogSetGroupDescrRequest request);
}
