package com.example.timecraft.domain.logEntry.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.logEntry.dto.LogEntryCreateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryCreateResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryGetResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryListAllResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateResponse;
import com.example.timecraft.domain.logEntry.mapper.LogEntityMapper;
import com.example.timecraft.domain.logEntry.persistence.LogEntryEntity;
import com.example.timecraft.domain.logEntry.persistence.LogEntryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LogEntryServiceImpl implements LogEntryService {
  private final LogEntryRepository repository;
  private final LogEntityMapper mapper;

  @Override
  public LogEntryListAllResponse listAll() {
    final List<LogEntryEntity> logEntryEntityList = repository.findAll();
    final List<LogEntryListAllResponse.LogEntryDto> logEntryDtoList = logEntryEntityList.stream().map(mapper::toListItem).toList();
    return new LogEntryListAllResponse(logEntryDtoList);
  }

  @Override
  public LogEntryCreateResponse create(final LogEntryCreateRequest request) {
    validateStartTime(request.getStartTime());
    LogEntryEntity logEntryEntity = mapper.fromCreateRequest(request);
    logEntryEntity.setDate(LocalDate.now());
    logEntryEntity = repository.save(logEntryEntity);
    return mapper.toCreateResponse(logEntryEntity);
  }

  private void validateStartTime(final LocalDateTime startTime) {
    if (startTime == null) {
      throw new BadRequestException("Start time must be provided");
    }
  }

  @Override
  public LogEntryGetResponse get(final long logEntryId) {
    final LogEntryEntity logEntryEntity = getRaw(logEntryId);
    return mapper.toGetResponse(logEntryEntity);
  }

  private LogEntryEntity getRaw(final long logEntryId) {
    return repository.findById(logEntryId)
        .orElseThrow(() -> new NotFoundException("Log entry with such id does not exist"));
  }

  @Override
  public LogEntryUpdateResponse update(final long logEntryId, final LogEntryUpdateRequest request) {
    validateStartTime(request.getStartTime());
    LogEntryEntity entity = getRaw(logEntryId);
    mapper.fromUpdateRequest(request, entity);
    if (request.getEndTime() != null) {
      entity.setTimeSpentSeconds((int) Duration.between(entity.getStartTime(), entity.getEndTime()).toSeconds());
    }
    entity = repository.save(entity);
    return mapper.toUpdateResponse(entity);
  }

  @Override
  public void delete(final long logEntryId) {
    final LogEntryEntity entity = getRaw(logEntryId);
    repository.delete(entity);
  }
}
