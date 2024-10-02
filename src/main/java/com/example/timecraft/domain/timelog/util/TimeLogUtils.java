package com.example.timecraft.domain.timelog.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import com.example.timecraft.core.exception.BadRequestException;

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

  public static int getDurationInSecondsForTimelog(final LocalTime startTime, final LocalTime endTime) {
    int duration = (int) Duration.between(startTime, endTime).toSeconds();
    return duration < 0 ? 3600 * 24 + duration : duration;
  }

  public static LocalDate getProcessedDate(final LocalDate date, final LocalTime startTime, final int offset) {
    if (startTime == null) return date;
    return startTime.isBefore(LocalTime.of(offset, 0))
        ? date.minusDays(1)
        : date;
  }

  public static String generateColor(String ticket, String descr) {
    try {
      String input = ticket + descr;

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

      int red = (Byte.toUnsignedInt(hash[0]) + Byte.toUnsignedInt(hash[13]) + Byte.toUnsignedInt(hash[29])) % 256;
      int green = (Byte.toUnsignedInt(hash[1]) + Byte.toUnsignedInt(hash[14]) + Byte.toUnsignedInt(hash[30])) % 256;
      int blue = (Byte.toUnsignedInt(hash[2]) + Byte.toUnsignedInt(hash[15]) + Byte.toUnsignedInt(hash[31])) % 256;

      int alpha = (int) (0.075 * 255);

      return String.format("#%02x%02x%02x%02x", red, green, blue, alpha);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error generating color", e);
    }

  }
}
