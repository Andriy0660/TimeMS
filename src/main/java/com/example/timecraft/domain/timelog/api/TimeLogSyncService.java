package com.example.timecraft.domain.timelog.api;

import java.time.LocalDate;
import java.util.List;

import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;

public interface TimeLogSyncService {
  List<TimeLogEntity> getAllByDate(final LocalDate date);

  List<TimeLogEntity> getAllByDateAndDescription(final LocalDate date, final String description);

  List<TimeLogEntity> getAllByDateAndDescriptionAndTicket(final LocalDate date, final String description, final String ticket);

  void saveAll(final List<TimeLogEntity> entities);

  void delete(final List<TimeLogEntity> entities);

}
