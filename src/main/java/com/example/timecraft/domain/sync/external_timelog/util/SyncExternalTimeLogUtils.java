package com.example.timecraft.domain.sync.external_timelog.util;

import java.util.List;

import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.example.timecraft.domain.timelog.util.DurationUtils;


public class SyncExternalTimeLogUtils {
  public static int getTotalSpentSecondsForExternalTimeLogs(final List<ExternalTimeLogEntity> entities) {
    return entities.stream().map(entity ->
            (int) DurationUtils.getDurationBetweenStartAndEndTime(entity.getStartTime(), entity.getEndTime()).toSeconds())
        .reduce(0, Integer::sum);
  }
}
