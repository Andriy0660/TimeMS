package com.example.timecraft.domain.worklog.service;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

@Service
public class SyncProgressService {
  private final AtomicReference<Double> progress = new AtomicReference<>(0.0);
  private final AtomicReference<String> ticketOfCurrentWorklog = new AtomicReference<>(null);
  private final AtomicReference<String> commentOfCurrentWorklog = new AtomicReference<>(null);

  public double getProgress() {
    return progress.get();
  }

  public void setProgress(double value) {
    progress.set(value);
  }

  public void clearProgress() {
    progress.set(0.0);
  }

  public String getTicketOfCurrentWorklog() {
    return ticketOfCurrentWorklog.get();
  }

  public void setTicketOfCurrentWorklog(final String ticketOfCurrentWorklog) {
    this.ticketOfCurrentWorklog.set(ticketOfCurrentWorklog);
  }

  public String getCommentOfCurrentWorklog() {
    return commentOfCurrentWorklog.get();
  }

  public void setCommentOfCurrentWorklog(final String commentOfCurrentWorklog) {
    this.commentOfCurrentWorklog.set(commentOfCurrentWorklog);
  }
}