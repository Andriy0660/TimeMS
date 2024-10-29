package com.example.timecraft.domain.sync.upwork.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.sync.upwork.model.UpworkSyncInfo;
import com.example.timecraft.domain.sync.upwork.persistence.UpworkSyncInfoEntity;
import com.example.timecraft.domain.sync.upwork.persistence.UpworkSyncInfoRepository;
import com.example.timecraft.domain.sync.upwork.util.SyncUpworkUtils;
import com.example.timecraft.domain.timelog.api.TimeLogSyncService;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekWithTicketsResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogWeekResponse;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncUpworkProcessingServiceImpl implements SyncUpworkProcessingService {
  private final TimeLogSyncService timeLogSyncService;
  private final UpworkSyncInfoRepository upworkSyncInfoRepository;

  @Override
  public TimeLogWeekResponse processWeekDayInfos(final TimeLogHoursForWeekWithTicketsResponse response) {
    final List<TimeLogHoursForWeekWithTicketsResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForWeekWithTicketsResponse(
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setUpworkSyncInfo(getUpworkSyncInfo(dayInfo.getDate())))
            .toList());
  }

  @Override
  public TimeLogWeekResponse processWeekDayInfos(final TimeLogHoursForWeekResponse response) {
    final List<TimeLogHoursForWeekResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForWeekResponse(
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setUpworkSyncInfo(getUpworkSyncInfo(dayInfo.getDate())))
            .toList());
  }

  @Override
  public TimeLogHoursForMonthResponse processMonthDayInfos(final TimeLogHoursForMonthResponse response) {
    final List<TimeLogHoursForMonthResponse.DayInfo> dayInfos = response.getItems();
    return new TimeLogHoursForMonthResponse(response.getTotalHours(),
        dayInfos.stream()
            .peek(dayInfo -> dayInfo.setUpworkSyncInfo(getUpworkSyncInfo(dayInfo.getDate())))
            .toList());
  }

  private UpworkSyncInfo getUpworkSyncInfo(final LocalDate date) {
    final int timeLogsSpentSeconds = TimeLogUtils.getTotalSpentSecondsForTimeLogs(timeLogSyncService.getAllByDate(date));
    final int upworkSpentSeconds = upworkSyncInfoRepository.findById(date).map(UpworkSyncInfoEntity::getTimeSpentSeconds).orElse(0);
    return new UpworkSyncInfo(SyncUpworkUtils.getSyncStatus(upworkSpentSeconds, timeLogsSpentSeconds));
  }

}
