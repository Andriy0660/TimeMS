package com.example.timecraft.domain.timelog.api;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.config.api.UserConfigService;
import com.example.timecraft.domain.sync.jira.util.SyncJiraUtils;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.persistence.TimeLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeLogSyncServiceImpl implements TimeLogSyncService {
  private final TimeLogRepository repository;
  private final UserConfigService userConfigService;

  @Override
  public List<TimeLogEntity> getAllByDate(final LocalDate date) {
    final int offset = userConfigService.getOffsetHour();
    final LocalTime startTime = LocalTime.of(offset, 0);
    return repository.findAllInRange(date, date.plusDays(1), startTime);
  }

  @Override
  public List<TimeLogEntity> getAllByDateAndDescription(final LocalDate date, final String description) {
    final int offset = userConfigService.getOffsetHour();
    return repository.findAllByDateAndDescription(date, date.plusDays(1), LocalTime.of(offset, 0), description);
  }

  @Override
  public List<TimeLogEntity> getAllByDateAndDescriptionAndTicket(final LocalDate date, final String description, final String ticket) {
    final int offset = userConfigService.getOffsetHour();
    return repository.findAllByDateAndTicket(date, date.plusDays(1), LocalTime.of(offset, 0), ticket).stream()
        .filter(timeLogEntity -> SyncJiraUtils.areDescriptionsEqual(timeLogEntity.getDescription(), description))
        .toList();
  }

  @Override
  public void saveAll(final List<TimeLogEntity> entities) {
    repository.saveAll(entities);
  }

  @Override
  public void delete(final List<TimeLogEntity> entities) {
    repository.deleteAll(entities);
  }

}
