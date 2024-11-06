package com.example.timecraft.domain.sync.external_service.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.external_timelog.api.ExternalTimeLogSyncService;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.example.timecraft.domain.sync.external_service.dto.SyncIntoExternalServiceRequest;
import com.example.timecraft.domain.timelog.api.TimeLogSyncService;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SyncExternalTimeLogServiceImpl implements SyncExternalTimeLogService {
  private final TimeLogSyncService timeLogSyncService;
  private final ExternalTimeLogSyncService externalTimeLogSyncService;

  @Override
  public void syncIntoExternalService(final SyncIntoExternalServiceRequest request) {
    final LocalDate date = request.getDate();
    final String description = request.getDescription();
    final List<TimeLogEntity> timeLogEntityList = timeLogSyncService.getAllByDateAndDescription(date, description);
    final List<ExternalTimeLogEntity> externalTimeLogEntities = externalTimeLogSyncService.getAllByDateAndDescription(date, description);

    final int totalSpentSeconds = TimeLogUtils.getTotalSpentSecondsForTimeLogs(timeLogEntityList);

    if (externalTimeLogEntities.isEmpty()) {
      throw new BadRequestException("There is no external timelogs");
    }

    deleteExternalTimeLogs(externalTimeLogEntities.stream().skip(1).toList());
    final ExternalTimeLogEntity first = externalTimeLogEntities.getFirst();
    first.setEndTime(first.getStartTime().plusSeconds(totalSpentSeconds));
    externalTimeLogSyncService.save(first);
  }

  private void deleteExternalTimeLogs(final List<ExternalTimeLogEntity> externalTimeLogEntities) {
    externalTimeLogEntities.forEach(externalTimeLog -> {
      externalTimeLogSyncService.deleteById(externalTimeLog.getId());
    });
  }
}
