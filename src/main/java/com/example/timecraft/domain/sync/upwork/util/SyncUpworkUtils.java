package com.example.timecraft.domain.sync.upwork.util;

import com.example.timecraft.domain.sync.model.SyncStatus;

public class SyncUpworkUtils {
  public static SyncStatus getSyncStatus(final Integer upworkSpentSeconds, final Integer timeLogsSpentSeconds) {
    if (upworkSpentSeconds != null) {
      if (timeLogsSpentSeconds.equals(upworkSpentSeconds)) {
        return SyncStatus.SYNCED;
      } else {
        return SyncStatus.NOT_SYNCED;
      }
    } else {
      return SyncStatus.NOT_SYNCED;
    }
  }

}
