package com.example.timecraft.domain.timelog.service;

import java.time.LocalDate;

import com.example.timecraft.domain.timelog.dto.TimeLogChangeDateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogMergeRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;

public interface TimeLogService {
  TimeLogListResponse list(final String mode, final LocalDate date, final int offset);

  TimeLogCreateResponse create(final TimeLogCreateRequest request);

  void merge(final TimeLogMergeRequest request);

  TimeLogGetResponse get(final long timeLogId);

  TimeLogHoursForWeekResponse getHoursForWeek(final LocalDate date, final int offset);

  TimeLogUpdateResponse update(final long timeLogId, final TimeLogUpdateRequest request);

  void delete(final long timeLogId);

  void setGroupDescription(final TimeLogSetGroupDescrRequest request);

  void changeDate(final long timeLogId, final TimeLogChangeDateRequest isNext);

  TimeLogHoursForMonthResponse getHoursForMonth(final LocalDate date, final int offset);
}
