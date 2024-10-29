package com.example.timecraft.domain.sync.upwork.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.sync.upwork.dto.SyncUpworkRequest;
import com.example.timecraft.domain.sync.upwork.dto.SyncUpworkStatusForDayResponse;
import com.example.timecraft.domain.sync.upwork.model.UpworkSyncInfo;
import com.example.timecraft.domain.sync.upwork.persistence.UpworkSyncInfoEntity;
import com.example.timecraft.domain.sync.upwork.persistence.UpworkSyncInfoRepository;
import com.example.timecraft.domain.sync.upwork.util.SyncUpworkUtils;
import com.example.timecraft.domain.timelog.api.TimeLogSyncService;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncUpworkServiceImpl implements SyncUpworkService {
  private final UpworkSyncInfoRepository repository;
  private final TimeLogSyncService timeLogSyncService;

  @Override
  public SyncUpworkStatusForDayResponse getSyncStatusForDay(final LocalDate date) {
    final int timeLogsSpentSeconds = TimeLogUtils.getTotalSpentSecondsForTimeLogs(timeLogSyncService.getAllByDate(date));
    final int upworkSpentSeconds = repository.findById(date).map(UpworkSyncInfoEntity::getTimeSpentSeconds).orElse(0);
    return new SyncUpworkStatusForDayResponse(date, new UpworkSyncInfo(SyncUpworkUtils.getSyncStatus(upworkSpentSeconds, timeLogsSpentSeconds)));
  }

  @Override
  public void sync(final SyncUpworkRequest request) {
    UpworkSyncInfoEntity entity = UpworkSyncInfoEntity.builder()
        .date(request.getDate())
        .timeSpentSeconds(request.getTimeSpentSeconds())
        .build();

    repository.save(entity);
  }
}
