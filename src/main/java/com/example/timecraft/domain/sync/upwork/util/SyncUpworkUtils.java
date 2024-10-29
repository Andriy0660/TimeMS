package com.example.timecraft.domain.sync.upwork.util;

import com.example.timecraft.domain.sync.model.SyncStatus;

public class SyncUpworkUtils {
  public static SyncStatus getSyncStatus(final int upworkSpentSeconds, final int timeLogsSpentSeconds) {
    if (timeLogsSpentSeconds == upworkSpentSeconds) {
      return SyncStatus.SYNCED;
    } else {
      return SyncStatus.NOT_SYNCED;
    }
  }
}
