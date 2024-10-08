package com.example.timecraft.domain.worklog.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

public interface WorklogSyncService {
  List<WorklogEntity> getAll();

  Optional<WorklogEntity> getById(final Long id);

  List<WorklogEntity> getAllByTicket(final String ticket);

  List<WorklogEntity> getAllByDate(final LocalDate date);

  List<WorklogEntity> getAllByDateAndCommentAndTicket(final LocalDate date, final String comment, final String ticket);

  void save(final WorklogEntity entity);

  void delete(final WorklogEntity entity);

}
