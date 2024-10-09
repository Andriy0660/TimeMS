package com.example.timecraft.domain.timelog.util;

import java.time.Duration;
import java.time.LocalTime;

public class DurationUtils {
  public static String formatDurationHM(final Duration duration) {
    return String.format("%dh %dm", duration.toHours(), duration.toMinutesPart());
  }

  public static String formatDurationHMS(final Duration duration) {
    return String.format("%dh %dm %ds", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
  }

  public static String formatDurationDH(final Duration duration) {
    return String.format("%dd %dh", duration.toDays(), duration.toHoursPart());
  }

  public static int getDurationInSecondsForTimelog(final LocalTime startTime, final LocalTime endTime) {
    if (startTime == null || endTime == null) return 0;
    final int duration = (int) Duration.between(startTime, endTime).toSeconds();
    return duration < 0 ? 3600 * 24 + duration : duration;
  }
}
