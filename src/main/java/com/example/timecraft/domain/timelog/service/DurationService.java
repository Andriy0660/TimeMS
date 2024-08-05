package com.example.timecraft.domain.timelog.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

@Service
public class DurationService {
  public static String formatDuration(final Duration duration) {
    return String.format("%dh %dm", duration.toHours(), duration.toMinutesPart());
  }
}
