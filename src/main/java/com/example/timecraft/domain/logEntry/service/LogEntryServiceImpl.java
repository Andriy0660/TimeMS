package com.example.timecraft.domain.logEntry.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    final List<LogEntryListAllResponse.LogEntryDto> logEntryDtoList = logEntryEntityList.stream()
        .map(mapper::toListItem)
        .sorted(
            Comparator.comparing(
                LogEntryListAllResponse.LogEntryDto::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())
            ).thenComparing(LogEntryListAllResponse.LogEntryDto::getId))
        .toList();
    return new LogEntryListAllResponse(logEntryDtoList);
  }

  @Override
  public LogEntryCreateResponse create(final LogEntryCreateRequest request) {
    LogEntryEntity logEntryEntity = mapper.fromCreateRequest(request);
    logEntryEntity.setDate(LocalDate.now());

    stopOtherLogEntries();

    logEntryEntity = repository.save(logEntryEntity);
    LogEntryCreateResponse response = mapper.toCreateResponse(logEntryEntity);
    if (isConflictedWithOthersLogEntries(null, request.getStartTime(), null)) {
      response.setConflicted(true);
    }
    return response;
  }

  private boolean isConflictedWithOthersLogEntries(final Long logEntryId, final LocalDateTime startTime, final LocalDateTime endTime) {
    final List<LogEntryEntity> logEntryEntities = repository.findAll();
    return logEntryEntities.stream().anyMatch(logEntry ->
        !logEntry.getId().equals(logEntryId) &&
            areIntervalsOverlapping(startTime, endTime, logEntry.getStartTime(), logEntry.getEndTime())
    );
  }

  private boolean areIntervalsOverlapping(final LocalDateTime startTime1, final LocalDateTime endTime1,
                                          final LocalDateTime startTime2, final LocalDateTime endTime2) {
    return
        isInInterval(startTime1, endTime1, startTime2) ||
        isInInterval(startTime1, endTime1, endTime2) ||
        isInInterval(startTime2, endTime2, startTime1) ||
        isInInterval(startTime2, endTime2, endTime1);
  }
  private boolean isInInterval(final LocalDateTime border1, final LocalDateTime border2, final LocalDateTime target) {
     if (target == null || border1 == null || border2 == null) {
      return false;
    }
    return target.isAfter(border1) && target.isBefore(border2);
  }

  private void stopOtherLogEntries() {
    repository.findAllByEndTimeIsNull().forEach((logEntry) -> {
      logEntry.setEndTime(LocalDateTime.now().withSecond(0).withNano(0));
      processSpentTime(logEntry);
      repository.save(logEntry);
    });
  }

  private void processSpentTime(final LogEntryEntity entity) {
    if (entity.getEndTime() != null) {
      entity.setTimeSpentSeconds((int) Duration.between(entity.getStartTime(), entity.getEndTime()).toSeconds());
    } else {
      entity.setTimeSpentSeconds(null);
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
    if(request.getEndTime() == null) {
      stopOtherLogEntries();
    }

    LogEntryEntity entity = getRaw(logEntryId);
    mapper.fromUpdateRequest(request, entity);
    processSpentTime(entity);
    entity = repository.save(entity);

    LogEntryUpdateResponse response = mapper.toUpdateResponse(entity);
    if (isConflictedWithOthersLogEntries(logEntryId, entity.getStartTime(), entity.getEndTime())) {
      response.setConflicted(true);
    }
    return response;
  }

  @Override
  public void delete(final long logEntryId) {
    final LogEntryEntity entity = getRaw(logEntryId);
    repository.delete(entity);
  }
}
