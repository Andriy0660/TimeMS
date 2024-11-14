package com.example.timecraft.domain.external_timelog.api;

import java.time.LocalDate;
import java.util.List;

import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;

public interface ExternalTimeLogSyncService {
  List<ExternalTimeLogEntity> getAllByDate(final LocalDate date);

  List<ExternalTimeLogEntity> getAllByDateAndDescription(final LocalDate date, final String description);

  void save(final ExternalTimeLogEntity externalTimeLogEntity);

  void deleteById(final Long id);
}
