package com.example.timecraft.domain.worklog.service;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import lombok.Data;

@Service
@Data
public class SyncProgressService {
  private final AtomicReference<Double> progress = new AtomicReference<>(0.0);
  private String ticketOfCurrentWorklog;
  private String commentOfCurrentWorklog;

  public double getProgress() {
    return progress.get();
  }

  public void setProgress(double value) {
    progress.set(value);
  }

  public void clearProgress() {
    progress.set(0.0);
  }
}