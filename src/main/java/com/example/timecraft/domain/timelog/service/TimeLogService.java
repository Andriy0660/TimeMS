package com.example.timecraft.domain.timelog.service;

import java.time.LocalDate;
import java.util.List;

import com.example.timecraft.domain.timelog.dto.TimeLogChangeDateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogConfigResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFormWorklogResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFromWorklogRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogImportRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;

public interface TimeLogService {
  TimeLogListResponse list(final String mode, final LocalDate date);

  List<TimeLogEntity> findAllByDateAndDescriptionAndTicket(final LocalDate date, final String description, final String ticket);

  List<TimeLogEntity> getAllTimeLogEntitiesInMode(final String mode, final LocalDate date, final int offset);

  void saveAll(final List<TimeLogEntity> entities);

  TimeLogCreateResponse create(final TimeLogCreateRequest request);

  TimeLogCreateFormWorklogResponse createFromWorklog(final TimeLogCreateFromWorklogRequest request);

  void importTimeLogs(final TimeLogImportRequest request);

  void divide(final long timeLogId);

  TimeLogGetResponse get(final long timeLogId);

  TimeLogConfigResponse getConfig();

  TimeLogHoursForWeekResponse getHoursForWeek(final LocalDate date);

  TimeLogUpdateResponse update(final long timeLogId, final TimeLogUpdateRequest request);

  void delete(final long timeLogId);

  void delete(final List<TimeLogEntity> entities);

  void setGroupDescription(final TimeLogSetGroupDescrRequest request);

  void changeDate(final long timeLogId, final TimeLogChangeDateRequest isNext);

  TimeLogHoursForMonthResponse getHoursForMonth(final LocalDate date);
}
