package com.example.timecraft.domain.timelog.util;

import java.awt.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.stream.Collectors;

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
    String input = descr + ticket.chars()
        .filter(Character::isDigit)
        .mapToObj(c -> String.valueOf((char) c))
        .collect(Collectors.joining());

    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-512");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    byte[] hashBytes = digest.digest(input.getBytes());

    int hueSum = 0;
    int saturationSum = 0;
    int brightnessSum = 0;

    for (int i = 0; i < hashBytes.length; i++) {
      int value = hashBytes[i] & 0xFF;
      hueSum += value * (i + 1);
      if (i % 3 == 0) saturationSum += value;
      if (i % 5 == 0) brightnessSum += value;
    }

    float hue = (hueSum % 360) / 360f;
    float saturation = 0.2f + (saturationSum % 500) / 1000f;
    float brightness = 0.5f + (brightnessSum % 300) / 1000f;

    int rgb = Color.HSBtoRGB(hue, saturation, brightness);

    int red = (rgb >> 16) & 0xFF;
    int green = (rgb >> 8) & 0xFF;
    int blue = rgb & 0xFF;

    return String.format("#%02x%02x%02x", red, green, blue);
  }
}
