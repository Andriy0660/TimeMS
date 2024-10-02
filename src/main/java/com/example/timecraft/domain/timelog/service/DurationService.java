package com.example.timecraft.domain.timelog.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

@Service
public class DurationService {
  public static String formatDurationHM(final Duration duration) {
    return String.format("%dh %dm", duration.toHours(), duration.toMinutesPart());
  }

  public static String formatDurationHMS(final Duration duration) {
    return String.format("%dh %dm %ds", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
  }

  public static String formatDurationDH(final Duration duration) {
    return String.format("%dd %dh", duration.toDays(), duration.toHoursPart());
  }

}
