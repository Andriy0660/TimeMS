package com.example.timecraft.domain.sync.service;

import com.example.timecraft.domain.sync.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;

public interface SyncService {
  void syncFromJira(SyncFromJiraRequest request);

  void syncIntoJira(SyncIntoJiraRequest request);

  TimeLogListResponse processTimeLogDtos(TimeLogListResponse response);

  TimeLogHoursForWeekResponse processWeekDayInfos(TimeLogHoursForWeekResponse response);

  TimeLogHoursForMonthResponse processMonthDayInfos(TimeLogHoursForMonthResponse response);

  WorklogListResponse processWorklogDtos(WorklogListResponse response);
}
