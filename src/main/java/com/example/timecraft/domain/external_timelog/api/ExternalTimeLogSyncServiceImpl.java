package com.example.timecraft.domain.external_timelog.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExternalTimeLogSyncServiceImpl implements ExternalTimeLogSyncService {
  private final ExternalTimeLogRepository repository;

  @Override
  public List<ExternalTimeLogEntity> getAllByDate(final LocalDate date) {
    return repository.findAllByDate(date);
  }

  @Override
  public List<ExternalTimeLogEntity> getAllByDateAndDescription(final LocalDate date, final String description) {
    return repository.findAllByDateAndDescription(date, description);
  }

  @Override
  public void save(final ExternalTimeLogEntity externalTimeLogEntity) {
    repository.save(externalTimeLogEntity);
  }

  @Override
  public void deleteById(final Long id) {
    repository.deleteById(id);
  }
}
