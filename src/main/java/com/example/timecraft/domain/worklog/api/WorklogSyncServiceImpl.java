package com.example.timecraft.domain.worklog.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.sync.jira.util.SyncJiraUtils;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorklogSyncServiceImpl implements WorklogSyncService {
  private final WorklogRepository repository;
  @Override
  public List<WorklogEntity> getAll() {
    return repository.findAll();
  }

  @Override
  public List<WorklogEntity> getAllByTicket(final String ticket) {
    return repository.findAllByTicket(ticket);
  }

  @Override
  public Optional<WorklogEntity> getById(final Long id) {
    return repository.findById(id);
  }

  @Override
  public List<WorklogEntity> getAllByDate(final LocalDate date) {
    return repository.findAllByDate(date);
  }

  @Override
  public List<WorklogEntity> getAllByDateAndCommentAndTicket(final LocalDate date, final String comment, final String ticket) {
    return repository.findAllByDateAndTicket(date, ticket).stream()
        .filter(worklogEntity -> SyncJiraUtils.areDescriptionsEqual(worklogEntity.getComment(), comment))
        .toList();
  }

  @Override
  public void save(final WorklogEntity entity) {
    repository.save(entity);
  }

  @Override
  public void delete(final WorklogEntity entity) {
    repository.delete(entity);
  }
}
