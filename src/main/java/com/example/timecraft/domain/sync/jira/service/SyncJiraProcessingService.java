package com.example.timecraft.domain.sync.jira.service;

import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;

public interface SyncJiraProcessingService {
  TimeLogListResponse processTimeLogDtos(TimeLogListResponse response);

  TimeLogHoursForWeekResponse processWeekDayInfos(TimeLogHoursForWeekResponse response);

  TimeLogHoursForMonthResponse processMonthDayInfos(TimeLogHoursForMonthResponse response);

  WorklogListResponse processWorklogDtos(WorklogListResponse response);
}
