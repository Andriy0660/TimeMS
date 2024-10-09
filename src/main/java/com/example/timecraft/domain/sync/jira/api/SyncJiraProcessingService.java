package com.example.timecraft.domain.sync.jira.api;

import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;

public interface SyncJiraProcessingService {
  TimeLogListResponse processTimeLogDtos(final TimeLogListResponse response);

  TimeLogHoursForWeekResponse processWeekDayInfos(final TimeLogHoursForWeekResponse response);

  TimeLogHoursForMonthResponse processMonthDayInfos(final TimeLogHoursForMonthResponse response);

  WorklogListResponse processWorklogDtos(final WorklogListResponse response);
}
