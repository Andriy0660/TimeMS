package com.example.timecraft.domain.timelog.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

}
