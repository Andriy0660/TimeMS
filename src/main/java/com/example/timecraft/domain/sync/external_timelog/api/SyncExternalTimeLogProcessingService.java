package com.example.timecraft.domain.sync.external_timelog.api;

import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekWithTicketsResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogWeekResponse;

public interface SyncExternalTimeLogProcessingService {
  TimeLogListResponse processTimeLogDtos(final TimeLogListResponse response);

  TimeLogWeekResponse processWeekDayInfos(final TimeLogHoursForWeekWithTicketsResponse response);

  TimeLogWeekResponse processWeekDayInfos(final TimeLogHoursForWeekResponse response);

  TimeLogHoursForMonthResponse processMonthDayInfos(final TimeLogHoursForMonthResponse response);

  ExternalTimeLogListResponse processExternalTimeLogDtos(final ExternalTimeLogListResponse response);

}
