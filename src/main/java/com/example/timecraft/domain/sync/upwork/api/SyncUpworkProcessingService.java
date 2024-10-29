package com.example.timecraft.domain.sync.upwork.api;

import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekWithTicketsResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogWeekResponse;

public interface SyncUpworkProcessingService {

  TimeLogWeekResponse processWeekDayInfos(final TimeLogHoursForWeekWithTicketsResponse response);

  TimeLogWeekResponse processWeekDayInfos(final TimeLogHoursForWeekResponse response);

  TimeLogHoursForMonthResponse processMonthDayInfos(final TimeLogHoursForMonthResponse response);
}
