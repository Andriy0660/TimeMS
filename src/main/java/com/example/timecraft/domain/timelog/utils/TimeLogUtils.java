package com.example.timecraft.domain.timelog.utils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

public class TimeLogUtils {
  public static LocalDate[] calculateDateRange(final String mode, final LocalDate date) {
    switch (mode) {
      case "Day" -> {
        return new LocalDate[]{date, date.plusDays(1)};
      }
      case "Week" -> {
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return new LocalDate[]{startOfWeek, endOfWeek.plusDays(1)};
      }
      case "Month" -> {
        LocalDate startOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());
        return new LocalDate[]{startOfMonth, endOfMonth.plusDays(1)};
      }
      case "All" -> {
        return null;
      }
      default -> throw new BadRequestException("Invalid time mode");
    }
  }

  public static boolean areDescriptionsEqual(String descr1, String descr2) {
    descr1 = descr1 != null ? removeNonLetterCharacters(descr1) : null;
    descr2 = descr2 != null ? removeNonLetterCharacters(descr2) : null;
    if (descr1 == null && descr2 == null) {
      return true;
    }
    return descr1 != null && descr1.equals(descr2);
  }

  public static boolean isWorklogsAndTimeLogsCompatibleInTime(final List<TimeLogEntity> timeLogs, final List<WorklogEntity> worklogs) {
    if(timeLogs.isEmpty() || worklogs.isEmpty()) {
      return false;
    }
    int totalTimeLogDurationInSeconds = timeLogs.stream().map(TimeLogUtils::getDurationInSecondsForTimelog).reduce(0, Integer::sum);
    int totalWorklogDurationInSeconds = worklogs.stream().map(WorklogEntity::getTimeSpentSeconds).reduce(0, Integer::sum);
    return totalTimeLogDurationInSeconds == totalWorklogDurationInSeconds;
  }

  public static int getDurationInSecondsForTimelog(final TimeLogEntity timeLogEntity) {
    int duration = (int) Duration.between(timeLogEntity.getStartTime(), timeLogEntity.getEndTime()).toSeconds();
    return duration < 0 ? 3600 * 24 + duration : duration;
  }

  public static String removeNonLetterCharacters(String input) {
    StringBuilder result = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isLetter(c)) {
        result.append(c);
      }
    }
    return result.toString();
  }

}
