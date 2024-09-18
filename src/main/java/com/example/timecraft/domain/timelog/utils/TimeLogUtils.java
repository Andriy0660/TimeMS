package com.example.timecraft.domain.timelog.utils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.fasterxml.jackson.databind.JsonNode;

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
    descr1 = descr1 != null ? removeNonLetterAndDigitCharacters(descr1) : null;
    descr2 = descr2 != null ? removeNonLetterAndDigitCharacters(descr2) : null;
    return Objects.equals(descr1, descr2);
  }

  public static boolean isWorklogsAndTimeLogsCompatibleInTime(final List<TimeLogEntity> timeLogs, final List<WorklogEntity> worklogs) {
    if(timeLogs.isEmpty() || worklogs.isEmpty()) {
      return false;
    }
    int totalTimeLogDurationInSeconds = timeLogs.stream()
        .map((timeLogEntity -> getDurationInSecondsForTimelog(timeLogEntity.getStartTime(), timeLogEntity.getEndTime())))
        .reduce(0, Integer::sum);
    int totalWorklogDurationInSeconds = worklogs.stream().map(WorklogEntity::getTimeSpentSeconds).reduce(0, Integer::sum);
    return totalTimeLogDurationInSeconds == totalWorklogDurationInSeconds;
  }

  public static int getDurationInSecondsForTimelog(final LocalTime startTime, final LocalTime endTime) {
    int duration = (int) Duration.between(startTime, endTime).toSeconds();
    return duration < 0 ? 3600 * 24 + duration : duration;
  }

  public static String removeNonLetterAndDigitCharacters(String input) {
    StringBuilder result = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isLetter(c) || Character.isDigit(c)) {
        result.append(c);
      }
    }
    return result.toString();
  }

  public static String getTextFromAdf(JsonNode node) {
    StringBuilder text = new StringBuilder();

    if (node.has("content")) {
      for (JsonNode content : node.get("content")) {
        String type = content.get("type").asText();

        switch (type) {
          case "hardBreak":
            text.append("\n");
            break;
          case "paragraph":
            if (!text.isEmpty()) {
              text.append("\n");
            }
            break;
          case "listItem":
            text.append("\n- ");
            break;
          case "text":
            String textContent = content.get("text").asText();
            if (content.has("marks")) {
              for (JsonNode mark : content.get("marks")) {
                if (mark.get("type").asText().equals("code")) {
                  textContent = "`" + textContent + "`";
                  break;
                }
              }
            }
            text.append(textContent);
            break;
          default:
            break;
        }

        if (content.has("content")) {
          text.append(getTextFromAdf(content));
        }
      }
    }
    return text.toString().trim();
  }
}
