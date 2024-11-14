package com.example.timecraft.domain.sync.jira.util;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

public class SyncJiraUtils {
  public static LocalTime DEFAULT_WORKLOG_START_TIME = LocalTime.of(10, 0);

  public static String getColorInputString(final String ticket, final String descr) {
    String input = "";
    if (descr != null) {
      input = descr;
    }
    if (ticket != null) {
      input = input.concat(ticket.chars()
          .filter(Character::isDigit)
          .mapToObj(c -> String.valueOf((char) c))
          .collect(Collectors.joining()));
    }
    return input;
  }

  public static boolean areDescriptionsEqual(String descr1, String descr2) {
    descr1 = descr1 != null ? removeNonLetterAndDigitCharacters(descr1) : "";
    descr2 = descr2 != null ? removeNonLetterAndDigitCharacters(descr2) : "";
    return (descr1.isEmpty() && descr2.isEmpty()) || Objects.equals(descr1, descr2);
  }

  public static boolean isWorklogsAndTimeLogsCompatibleInTime(final List<TimeLogEntity> timeLogs, final List<WorklogEntity> worklogs) {
    if (timeLogs.isEmpty() || worklogs.isEmpty()) {
      return false;
    }
    final int totalTimeLogDurationInSeconds = TimeLogUtils.getTotalSpentSecondsForTimeLogs(timeLogs);
    final int totalWorklogDurationInSeconds = worklogs.stream().map(WorklogEntity::getTimeSpentSeconds).reduce(0, Integer::sum);
    return totalTimeLogDurationInSeconds == totalWorklogDurationInSeconds;
  }

  public static String removeNonLetterAndDigitCharacters(String input) {
    if (input == null) return null;
    final StringBuilder result = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isLetter(c) || Character.isDigit(c)) {
        result.append(c);
      }
    }
    return result.toString();
  }
}
