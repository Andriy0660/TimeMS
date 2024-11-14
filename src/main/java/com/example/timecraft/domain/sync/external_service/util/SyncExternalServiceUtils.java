package com.example.timecraft.domain.sync.external_service.util;

import java.util.List;

import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.example.timecraft.domain.external_timelog.util.ExternalTimeLogUtils;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;


public class SyncExternalServiceUtils {

  public static boolean isExternalTimeLogsAndTimeLogsCompatibleInTime(final List<ExternalTimeLogEntity> externalTimeLogs, final List<TimeLogEntity> timeLogs) {
    if (timeLogs.isEmpty() || externalTimeLogs.isEmpty()) {
      return false;
    }
    final int totalTimeLogDurationInSeconds = TimeLogUtils.getTotalSpentSecondsForTimeLogs(timeLogs);
    final int totalWorklogDurationInSeconds = ExternalTimeLogUtils.getTotalSpentSecondsForExternalTimeLogs(externalTimeLogs);
    return totalTimeLogDurationInSeconds == totalWorklogDurationInSeconds;
  }
}
