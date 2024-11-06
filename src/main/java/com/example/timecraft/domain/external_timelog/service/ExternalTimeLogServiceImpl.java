package com.example.timecraft.domain.external_timelog.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogRequest;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogResponse;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogListResponse;
import com.example.timecraft.domain.external_timelog.mapper.ExternalTimeLogMapper;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ExternalTimeLogServiceImpl implements ExternalTimeLogService {
  private final ExternalTimeLogRepository repository;
  private final ExternalTimeLogMapper mapper;

  @Override
  public ExternalTimeLogListResponse list(final LocalDate date) {
    final List<ExternalTimeLogEntity> externalTimeLogEntities = repository.findAllByDate(date);
    final List<ExternalTimeLogListResponse.ExternalTimeLogDto> externalTimeLogDtos = externalTimeLogEntities.stream()
        .map(mapper::toListItem)
        .toList();
    return new ExternalTimeLogListResponse(externalTimeLogDtos);
  }

  @Override
  public ExternalTimeLogCreateFromTimeLogResponse createFromTimeLog(final ExternalTimeLogCreateFromTimeLogRequest request) {
    ExternalTimeLogEntity entity = mapper.fromCreateFromTimeLogRequest(request);
    entity.setDescription(entity.getDescription().trim());
    entity = repository.save(entity);
    return mapper.toCreateResponse(entity);
  }

  @Override
  public void delete(final Long id) {
    repository.deleteById(id);
  }
}
