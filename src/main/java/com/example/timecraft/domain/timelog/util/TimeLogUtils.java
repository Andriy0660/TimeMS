package com.example.timecraft.domain.timelog.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;

public class TimeLogUtils {

  public static LocalDate getProcessedDate(final LocalDate date, final LocalTime startTime, final int offset) {
    if (startTime == null) return date;
    return startTime.isBefore(LocalTime.of(offset, 0))
        ? date.minusDays(1)
        : date;
  }

  public static int getTotalSpentSecondsForTimeLogs(final List<TimeLogEntity> timeLogEntityList) {
    return timeLogEntityList.stream().map(timeLogEntity -> {
      if (timeLogEntity.getStartTime() == null || timeLogEntity.getEndTime() == null) return 0;
      return (int) DurationUtils.getDurationBetweenStartAndEndTime(timeLogEntity.getStartTime(), timeLogEntity.getEndTime()).toSeconds();
    }).reduce(0, Integer::sum);
  }
}
