package com.example.timecraft.domain.sync.util;

import java.util.List;
import java.util.Objects;

import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

public class SyncUtil {
  public static boolean areDescriptionsEqual(String descr1, String descr2) {
    descr1 = descr1 != null ? removeNonLetterAndDigitCharacters(descr1) : null;
    descr2 = descr2 != null ? removeNonLetterAndDigitCharacters(descr2) : null;
    return Objects.equals(descr1, descr2);
  }

  public static boolean isWorklogsAndTimeLogsCompatibleInTime(final List<TimeLogEntity> timeLogs, final List<WorklogEntity> worklogs) {
    if (timeLogs.isEmpty() || worklogs.isEmpty()) {
      return false;
    }
    int totalTimeLogDurationInSeconds = timeLogs.stream()
        .map((timeLogEntity -> TimeLogUtils.getDurationInSecondsForTimelog(timeLogEntity.getStartTime(), timeLogEntity.getEndTime())))
        .reduce(0, Integer::sum);
    int totalWorklogDurationInSeconds = worklogs.stream().map(WorklogEntity::getTimeSpentSeconds).reduce(0, Integer::sum);
    return totalTimeLogDurationInSeconds == totalWorklogDurationInSeconds;
  }

  public static String removeNonLetterAndDigitCharacters(String input) {
    if(input == null) return null;
    StringBuilder result = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isLetter(c) || Character.isDigit(c)) {
        result.append(c);
      }
    }
    return result.toString();
  }
}
