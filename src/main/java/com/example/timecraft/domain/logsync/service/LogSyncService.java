package com.example.timecraft.domain.logsync.service;

import com.example.timecraft.domain.logsync.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;

public interface LogSyncService {
  void syncFromJira(SyncFromJiraRequest request);

  TimeLogListResponse processTimeLogDtos(TimeLogListResponse response);

  TimeLogHoursForWeekResponse processWeekDayInfos(TimeLogHoursForWeekResponse response);

  TimeLogHoursForMonthResponse processMonthDayInfos(TimeLogHoursForMonthResponse response);

  WorklogListResponse processWorklogDtos(WorklogListResponse response);
}
