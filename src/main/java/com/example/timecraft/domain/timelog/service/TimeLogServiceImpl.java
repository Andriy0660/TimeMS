package com.example.timecraft.domain.timelog.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.mapper.TimeLogMapper;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.persistence.TimeLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeLogServiceImpl implements TimeLogService {
  private final TimeLogRepository repository;
  private final TimeLogMapper mapper;
  private final Clock clock;

  @Override
  public TimeLogListResponse list(LocalDate day) {
    final List<TimeLogEntity> timeLogEntityList = repository.findAllByDateIs(day);
    final List<TimeLogListResponse.TimeLogDto> timeLogDtoList = timeLogEntityList.stream()
        .map(mapper::toListItem)
        .sorted(
            Comparator.comparing(
                TimeLogListResponse.TimeLogDto::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())
            ).thenComparing(TimeLogListResponse.TimeLogDto::getId))
        .toList();
    return new TimeLogListResponse(timeLogDtoList);
  }

  @Override
  public TimeLogCreateResponse create(final TimeLogCreateRequest request) {
    TimeLogEntity timeLogEntity = mapper.fromCreateRequest(request);
    timeLogEntity.setDate(LocalDate.now(clock));

    if(timeLogEntity.getStartTime() != null) {
      stopOtherTimeLogs(null);
    }

    timeLogEntity = repository.save(timeLogEntity);
    TimeLogCreateResponse response = mapper.toCreateResponse(timeLogEntity);
    if (isConflictedWithOthersTimeLogs(null, request.getStartTime(), null)) {
      response.setConflicted(true);
    }
    return response;
  }

  private boolean isConflictedWithOthersTimeLogs(final Long logEntryId, final LocalDateTime startTime, final LocalDateTime endTime) {
    final List<TimeLogEntity> logEntryEntities = repository.findAll();
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
    return !target.isBefore(border1) && !target.isAfter(border2);
  }

  private void stopOtherTimeLogs(Long excludedId) {
    repository.findAllByEndTimeIsNull().forEach((logEntry) -> {
      if (logEntry.getId().equals(excludedId) || logEntry.getStartTime() == null) {
        return;
      }
      logEntry.setEndTime(LocalDateTime.now(clock).withSecond(0).withNano(0));
      repository.save(logEntry);
    });
  }


  @Override
  public TimeLogGetResponse get(final long logEntryId) {
    final TimeLogEntity timeLogEntity = getRaw(logEntryId);
    return mapper.toGetResponse(timeLogEntity);
  }

  private TimeLogEntity getRaw(final long logEntryId) {
    return repository.findById(logEntryId)
        .orElseThrow(() -> new NotFoundException("Log entry with such id does not exist"));
  }

  @Override
  public TimeLogUpdateResponse update(final long logEntryId, final TimeLogUpdateRequest request) {
    if (request.getEndTime() == null) {
      stopOtherTimeLogs(logEntryId);
    }

    TimeLogEntity entity = getRaw(logEntryId);
    mapper.fromUpdateRequest(request, entity);
    entity = repository.save(entity);

    TimeLogUpdateResponse response = mapper.toUpdateResponse(entity);
    if (isConflictedWithOthersTimeLogs(logEntryId, entity.getStartTime(), entity.getEndTime())) {
      response.setConflicted(true);
    }
    return response;
  }

  @Override
  public void delete(final long logEntryId) {
    final TimeLogEntity entity = getRaw(logEntryId);
    repository.delete(entity);
  }
}
